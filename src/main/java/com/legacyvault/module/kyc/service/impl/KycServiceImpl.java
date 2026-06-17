package com.legacyvault.module.kyc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.legacyvault.common.Constants;
import com.legacyvault.common.PageResult;
import com.legacyvault.common.ResultCode;
import com.legacyvault.config.LegacyVaultProperties;
import com.legacyvault.exception.BusinessException;
import com.legacyvault.mock.MockKycService;
import com.legacyvault.module.auth.service.AuditLogService;
import com.legacyvault.module.kyc.dto.KycRecordVo;
import com.legacyvault.module.kyc.dto.KycStatusResponse;
import com.legacyvault.module.kyc.dto.KycSubmitRequest;
import com.legacyvault.module.kyc.entity.KycRecord;
import com.legacyvault.module.kyc.mapper.KycRecordMapper;
import com.legacyvault.module.kyc.service.KycService;
import com.legacyvault.module.user.entity.User;
import com.legacyvault.module.user.mapper.UserMapper;
import com.legacyvault.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * KYC 服务实现
 *
 * @author LegacyVault
 */
@Slf4j
@Service
public class KycServiceImpl implements KycService {

    @Autowired
    private KycRecordMapper kycRecordMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MockKycService mockKycService;

    @Autowired
    private LegacyVaultProperties properties;

    @Autowired
    private AuditLogService auditLogService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(Long userId, KycSubmitRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (user.getKycStatus() != null
                && user.getKycStatus() == Constants.KYC_STATUS_PASSED) {
            throw new BusinessException("KYC 已通过，无需重复提交");
        }

        // 创建 KYC 单据
        KycRecord record = new KycRecord();
        record.setUserId(userId);
        record.setRealName(request.getRealName());
        record.setIdType(request.getIdType() != null ? request.getIdType() : "ID_CARD");
        // 证件号加密存储（简化：SHA-256 + salt 模拟 AES）
        record.setIdNoEncrypted(SecurityUtil.sha256(request.getIdNo()));
        record.setFrontImageUrl(request.getFrontImage());
        record.setBackImageUrl(request.getBackImage());
        record.setLivenessPassed(Boolean.TRUE.equals(request.getLivenessPassed()) ? 1 : 0);
        record.setProvider(properties.getMockModeEnabled() ? "MOCK" : getProviderFromConfig());

        // 调用第三方 KYC 服务（Mock / 真实双分支）
        Map<String, Object> result = mockKycService.submitKyc(userId, request.getRealName(), request.getIdNo());
        String status = (String) result.get("status");
        String providerRequestId = (String) result.get("kycId");
        record.setProviderRequestId(providerRequestId);

        if ("PASSED".equals(status)) {
            record.setStatus(Constants.KYC_RECORD_AUTO_PASSED);
            user.setKycStatus(Constants.KYC_STATUS_PASSED);
            user.setSecurityScore(Math.min(100, (user.getSecurityScore() != null ? user.getSecurityScore() : 0) + 30));
            user.setKycRejectReason(null);
        } else {
            record.setStatus(Constants.KYC_RECORD_PENDING_MANUAL);
            user.setKycStatus(Constants.KYC_STATUS_SUBMITTED);
        }

        kycRecordMapper.insert(record);
        userMapper.updateById(user);

        auditLogService.log(userId, Constants.AUDIT_MODULE_KYC, "submit",
                String.format("{\"recordId\":%d,\"result\":\"%s\"}", record.getId(), status));
        log.info("KYC 提交 | userId={} | recordId={} | result={}", userId, record.getId(), status);
    }

    @Override
    public KycStatusResponse getStatus(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        KycRecord latest = kycRecordMapper.selectOne(
                new LambdaQueryWrapper<KycRecord>()
                        .eq(KycRecord::getUserId, userId)
                        .orderByDesc(KycRecord::getCreatedAt)
                        .last("LIMIT 1"));

        KycStatusResponse resp = new KycStatusResponse();
        resp.setStatus(user.getKycStatus() != null ? user.getKycStatus() : Constants.KYC_STATUS_NONE);
        resp.setStatusText(resolveKycStatusText(resp.getStatus()));
        resp.setRejectReason(user.getKycRejectReason());
        if (latest != null) {
            resp.setRecordId(latest.getId());
            resp.setProvider(latest.getProvider());
            resp.setSubmittedAt(latest.getCreatedAt());
            resp.setReviewedAt(latest.getReviewedAt());
        }
        return resp;
    }

    @Override
    public PageResult<KycRecordVo> listPending(int page, int size) {
        Page<KycRecord> pageParam = new Page<>(page, size);
        Page<KycRecord> result = kycRecordMapper.selectPage(pageParam,
                new LambdaQueryWrapper<KycRecord>()
                        .eq(KycRecord::getStatus, Constants.KYC_RECORD_PENDING_MANUAL)
                        .orderByAsc(KycRecord::getCreatedAt));
        return toPageResult(result);
    }

    @Override
    public PageResult<KycRecordVo> listAll(int page, int size, Integer status) {
        Page<KycRecord> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<KycRecord> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(KycRecord::getStatus, status);
        }
        wrapper.orderByDesc(KycRecord::getCreatedAt);
        Page<KycRecord> result = kycRecordMapper.selectPage(pageParam, wrapper);
        return toPageResult(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long recordId, Long reviewerId) {
        KycRecord record = kycRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "KYC 单据不存在");
        }
        if (record.getStatus() != Constants.KYC_RECORD_PENDING_MANUAL) {
            throw new BusinessException("当前状态不允许审核");
        }

        record.setStatus(Constants.KYC_RECORD_MANUAL_PASSED);
        record.setReviewerId(reviewerId);
        record.setReviewedAt(LocalDateTime.now());
        record.setRejectReason(null);
        kycRecordMapper.updateById(record);

        User user = userMapper.selectById(record.getUserId());
        user.setKycStatus(Constants.KYC_STATUS_PASSED);
        user.setKycRejectReason(null);
        user.setSecurityScore(Math.min(100, (user.getSecurityScore() != null ? user.getSecurityScore() : 0) + 30));
        userMapper.updateById(user);

        auditLogService.log(null, Constants.AUDIT_MODULE_KYC, "admin_approve",
                String.format("{\"recordId\":%d,\"reviewerId\":%d}", recordId, reviewerId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long recordId, Long reviewerId, String rejectReason) {
        KycRecord record = kycRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "KYC 单据不存在");
        }
        if (record.getStatus() != Constants.KYC_RECORD_PENDING_MANUAL) {
            throw new BusinessException("当前状态不允许审核");
        }

        record.setStatus(Constants.KYC_RECORD_MANUAL_REJECTED);
        record.setReviewerId(reviewerId);
        record.setReviewedAt(LocalDateTime.now());
        record.setRejectReason(rejectReason);
        kycRecordMapper.updateById(record);

        User user = userMapper.selectById(record.getUserId());
        user.setKycStatus(Constants.KYC_STATUS_REJECTED);
        user.setKycRejectReason(rejectReason);
        userMapper.updateById(user);

        auditLogService.log(null, Constants.AUDIT_MODULE_KYC, "admin_reject",
                String.format("{\"recordId\":%d,\"reviewerId\":%d,\"reason\":\"%s\"}",
                        recordId, reviewerId, rejectReason));
    }

    private PageResult<KycRecordVo> toPageResult(Page<KycRecord> page) {
        PageResult<KycRecordVo> result = new PageResult<>();
        result.setTotal(page.getTotal());
        result.setPageNum(page.getCurrent());
        result.setPageSize(page.getSize());
        result.setTotalPages(page.getPages());
        result.setRecords(page.getRecords().stream().map(this::toVo).collect(Collectors.toList()));
        return result;
    }

    private KycRecordVo toVo(KycRecord record) {
        KycRecordVo vo = new KycRecordVo();
        vo.setId(record.getId());
        vo.setUserId(record.getUserId());
        vo.setRealName(record.getRealName());
        vo.setIdType(record.getIdType());
        // 证件号脱敏
        vo.setIdNoMasked(maskIdNo(record.getIdNoEncrypted()));
        vo.setLivenessPassed(record.getLivenessPassed());
        vo.setStatus(record.getStatus());
        vo.setStatusText(resolveRecordStatusText(record.getStatus()));
        vo.setProvider(record.getProvider());
        vo.setRejectReason(record.getRejectReason());
        vo.setReviewerId(record.getReviewerId());
        vo.setReviewedAt(record.getReviewedAt());
        vo.setCreatedAt(record.getCreatedAt());
        // 用户信息（邮箱/手机/昵称脱敏）
        User user = userMapper.selectById(record.getUserId());
        if (user != null) {
            vo.setUserEmail(maskEmail(user.getEmail()));
            vo.setUserPhone(maskPhone(user.getPhone()));
            vo.setUserNickname(user.getNickname());
        }
        return vo;
    }

    private String resolveKycStatusText(Integer status) {
        if (status == null) return "未认证";
        switch (status) {
            case Constants.KYC_STATUS_NONE: return "未认证";
            case Constants.KYC_STATUS_SUBMITTED: return "审核中";
            case Constants.KYC_STATUS_PASSED: return "已通过";
            case Constants.KYC_STATUS_REJECTED: return "已驳回";
            default: return "未知";
        }
    }

    private String resolveRecordStatusText(Integer status) {
        if (status == null) return "未提交";
        switch (status) {
            case Constants.KYC_RECORD_NONE: return "未提交";
            case Constants.KYC_RECORD_AUTO_PASSED: return "机审通过";
            case Constants.KYC_RECORD_AUTO_FAILED: return "机审失败";
            case Constants.KYC_RECORD_PENDING_MANUAL: return "待人工审核";
            case Constants.KYC_RECORD_MANUAL_PASSED: return "人工通过";
            case Constants.KYC_RECORD_MANUAL_REJECTED: return "人工驳回";
            default: return "未知";
        }
    }

    private String maskIdNo(String encrypted) {
        if (encrypted == null) return "***";
        if (encrypted.length() <= 6) return "***";
        return encrypted.substring(0, 3) + "****" + encrypted.substring(encrypted.length() - 3);
    }

    private String maskEmail(String email) {
        if (email == null) return "";
        int idx = email.indexOf('@');
        if (idx <= 2) return email;
        return email.substring(0, 2) + "***" + email.substring(idx);
    }

    private String maskPhone(String phone) {
        if (phone == null) return "";
        if (phone.length() < 7) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private String getProviderFromConfig() {
        // 真实模式：从 sys_config 读取（此处简化）
        return "JUMIO";
    }
}
