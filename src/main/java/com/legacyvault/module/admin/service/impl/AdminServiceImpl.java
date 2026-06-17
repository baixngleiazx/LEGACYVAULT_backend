package com.legacyvault.module.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.legacyvault.common.Constants;
import com.legacyvault.common.PageResult;
import com.legacyvault.common.ResultCode;
import com.legacyvault.exception.BusinessException;
import com.legacyvault.module.admin.dto.AdminDashboardVo;
import com.legacyvault.module.admin.dto.AdminUserListVo;
import com.legacyvault.module.admin.dto.AuditLogVo;
import com.legacyvault.module.admin.service.AdminService;
import com.legacyvault.module.auth.entity.AuditLog;
import com.legacyvault.module.auth.mapper.AuditLogMapper;
import com.legacyvault.module.kyc.entity.KycRecord;
import com.legacyvault.module.kyc.mapper.KycRecordMapper;
import com.legacyvault.module.user.dto.HeirResponse;
import com.legacyvault.module.user.entity.Heir;
import com.legacyvault.module.user.entity.User;
import com.legacyvault.module.user.mapper.HeirMapper;
import com.legacyvault.module.user.mapper.UserMapper;
import com.legacyvault.module.user.service.HeirService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理员服务实现
 *
 * @author LegacyVault
 */
@Slf4j
@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private HeirMapper heirMapper;

    @Autowired
    private KycRecordMapper kycRecordMapper;

    @Autowired
    private AuditLogMapper auditLogMapper;

    @Autowired
    private HeirService heirService;

    @Override
    public AdminDashboardVo dashboard() {
        AdminDashboardVo vo = new AdminDashboardVo();

        vo.setTotalUsers(userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getDeleted, 0)));

        // 注册进行中：5 步未全部完成（step5_done=0 且 step5_skipped=0）
        vo.setRegistrationInProgress(userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                        .eq(User::getDeleted, 0)
                        .and(w -> w.eq(User::getStep5Done, 0).or().isNull(User::getStep5Done))
                        .and(w -> w.eq(User::getStep5Skipped, 0).or().isNull(User::getStep5Skipped))));

        vo.setRegistrationCompleted(vo.getTotalUsers() - vo.getRegistrationInProgress());

        vo.setKycPending(kycRecordMapper.selectCount(
                new LambdaQueryWrapper<KycRecord>()
                        .eq(KycRecord::getStatus, Constants.KYC_RECORD_PENDING_MANUAL)));

        vo.setKycPassed(userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                        .eq(User::getDeleted, 0)
                        .eq(User::getKycStatus, Constants.KYC_STATUS_PASSED)));

        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        vo.setTodayActiveUsers(userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                        .eq(User::getDeleted, 0)
                        .ge(User::getLastLoginAt, startOfDay)));
        return vo;
    }

    @Override
    public PageResult<AdminUserListVo> listUsers(int page, int size, String keyword, Integer status) {
        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>().eq(User::getDeleted, 0);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(User::getEmail, keyword)
                    .or().like(User::getPhone, keyword)
                    .or().like(User::getNickname, keyword));
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByDesc(User::getCreatedAt);
        Page<User> result = userMapper.selectPage(pageParam, wrapper);

        PageResult<AdminUserListVo> pr = new PageResult<>();
        pr.setTotal(result.getTotal());
        pr.setPageNum(result.getCurrent());
        pr.setPageSize(result.getSize());
        pr.setTotalPages(result.getPages());
        pr.setRecords(result.getRecords().stream().map(this::toUserVo).collect(Collectors.toList()));
        return pr;
    }

    @Override
    public AdminUserListVo getUserDetail(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getDeleted() != null && user.getDeleted() == 1) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return toUserVo(user);
    }

    @Override
    public List<HeirResponse> getUserHeirs(Long userId) {
        // 只读查询，复用 HeirService
        return heirService.listHeirs(userId);
    }

    @Override
    public PageResult<AuditLogVo> listAuditLogs(int page, int size, String module, Long userId) {
        Page<AuditLog> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<>();
        if (module != null && !module.isEmpty()) {
            wrapper.eq(AuditLog::getModule, module);
        }
        if (userId != null) {
            wrapper.eq(AuditLog::getUserId, userId);
        }
        wrapper.orderByDesc(AuditLog::getCreatedAt);
        Page<AuditLog> result = auditLogMapper.selectPage(pageParam, wrapper);

        PageResult<AuditLogVo> pr = new PageResult<>();
        pr.setTotal(result.getTotal());
        pr.setPageNum(result.getCurrent());
        pr.setPageSize(result.getSize());
        pr.setTotalPages(result.getPages());
        pr.setRecords(result.getRecords().stream().map(this::toAuditVo).collect(Collectors.toList()));
        return pr;
    }

    private AdminUserListVo toUserVo(User user) {
        AdminUserListVo vo = new AdminUserListVo();
        vo.setId(user.getId());
        vo.setEmail(maskEmail(user.getEmail()));
        vo.setPhone(maskPhone(user.getPhone()));
        vo.setNickname(user.getNickname());
        vo.setStatus(user.getStatus());
        vo.setPlanId(user.getPlanId());
        vo.setPlanName(resolvePlanName(user.getPlanId()));
        vo.setSecurityScore(user.getSecurityScore());
        vo.setTotpBound(user.getTotpBound());
        vo.setBiometricBound(user.getBiometricBound());
        vo.setKycStatus(user.getKycStatus());
        vo.setKycStatusText(resolveKycStatusText(user.getKycStatus()));
        vo.setKycRejectReason(user.getKycRejectReason());
        vo.setStep1Done(user.getStep1Done());
        vo.setStep2Done(user.getStep2Done());
        vo.setStep3Done(user.getStep3Done());
        vo.setStep4Done(user.getStep4Done());
        vo.setStep5Done(user.getStep5Done());
        vo.setKycAssetThreshold(user.getKycAssetThreshold());
        vo.setLastLoginAt(user.getLastLoginAt());
        vo.setCreatedAt(user.getCreatedAt());
        // 继承人数量
        Long heirCount = heirMapper.selectCount(
                new LambdaQueryWrapper<Heir>().eq(Heir::getUserId, user.getId()));
        vo.setHeirCount(heirCount.intValue());
        return vo;
    }

    private AuditLogVo toAuditVo(AuditLog auditLog) {
        AuditLogVo vo = new AuditLogVo();
        vo.setId(auditLog.getId());
        vo.setUserId(auditLog.getUserId());
        vo.setModule(auditLog.getModule());
        vo.setAction(auditLog.getAction());
        vo.setTargetType(auditLog.getTargetType());
        vo.setTargetId(auditLog.getTargetId());
        vo.setDetail(auditLog.getDetail());
        vo.setIpAddress(auditLog.getIpAddress());
        vo.setUserAgent(auditLog.getUserAgent());
        vo.setCreatedAt(auditLog.getCreatedAt());
        return vo;
    }

    private String resolvePlanName(Long planId) {
        if (planId == null) return "Free";
        if (planId == Constants.PLAN_PRO) return "Pro";
        if (planId == Constants.PLAN_VAULT) return "Vault";
        return "Free";
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
}
