package com.legacyvault.module.delivery.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.legacyvault.common.Constants;
import com.legacyvault.common.ResultCode;
import com.legacyvault.config.LegacyVaultProperties;
import com.legacyvault.exception.BusinessException;
import com.legacyvault.mock.MockBlockchainService;
import com.legacyvault.mock.MockEmailService;
import com.legacyvault.mock.MockFaceRecognitionService;
import com.legacyvault.mock.MockSmsService;
import com.legacyvault.module.auth.entity.VerificationCode;
import com.legacyvault.module.auth.mapper.VerificationCodeMapper;
import com.legacyvault.module.auth.service.AuditLogService;
import com.legacyvault.module.content.entity.EncryptedContent;
import com.legacyvault.module.content.mapper.EncryptedContentMapper;
import com.legacyvault.module.delivery.dto.DeliveryContentResponse;
import com.legacyvault.module.delivery.dto.DeliveryLinkResponse;
import com.legacyvault.module.delivery.dto.DeliveryOtpRequest;
import com.legacyvault.module.delivery.dto.DeliveryVerifyRequest;
import com.legacyvault.module.delivery.entity.DeliveryAccessLog;
import com.legacyvault.module.delivery.entity.DeliveryLink;
import com.legacyvault.module.delivery.mapper.DeliveryAccessLogMapper;
import com.legacyvault.module.delivery.mapper.DeliveryLinkMapper;
import com.legacyvault.module.delivery.service.DeliveryService;
import com.legacyvault.module.user.entity.Heir;
import com.legacyvault.module.user.entity.HeirContentAssignment;
import com.legacyvault.module.user.mapper.HeirMapper;
import com.legacyvault.module.user.mapper.HeirContentAssignmentMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.legacyvault.util.SecurityUtil;

/**
 * 遗产交付服务实现
 * 处理继承人身份核验和零知识内容交付
 *
 * @author LegacyVault
 */
@Slf4j
@Service
public class DeliveryServiceImpl implements DeliveryService {

    @Autowired
    private DeliveryLinkMapper deliveryLinkMapper;

    @Autowired
    private DeliveryAccessLogMapper accessLogMapper;

    @Autowired
    private EncryptedContentMapper contentMapper;

    @Autowired
    private HeirMapper heirMapper;

    @Autowired
    private HeirContentAssignmentMapper assignmentMapper;

    @Autowired
    private VerificationCodeMapper verificationCodeMapper;

    @Autowired
    private MockFaceRecognitionService mockFaceRecognitionService;

    @Autowired
    private MockBlockchainService mockBlockchainService;

    @Autowired
    private MockEmailService mockEmailService;

    @Autowired
    private MockSmsService mockSmsService;

    @Autowired
    private LegacyVaultProperties properties;

    @Autowired
    private AuditLogService auditLogService;

    @Override
    public List<DeliveryLinkResponse> listDeliveryLinks(Long userId) {
        List<DeliveryLink> links = deliveryLinkMapper.selectList(
                new LambdaQueryWrapper<DeliveryLink>()
                        .eq(DeliveryLink::getUserId, userId)
                        .orderByDesc(DeliveryLink::getCreatedAt));

        return links.stream().map(link -> {
            DeliveryLinkResponse response = new DeliveryLinkResponse();
            response.setId(link.getId());
            response.setHeirId(link.getHeirId());
            response.setLinkToken(link.getLinkToken());
            response.setStatus(link.getStatus());

            String[] statusTexts = {"已失效", "有效", "已使用", "已锁定"};
            if (link.getStatus() >= 0 && link.getStatus() < statusTexts.length) {
                response.setStatusText(statusTexts[link.getStatus()]);
            }

            // 查询继承人姓名
            Heir heir = heirMapper.selectById(link.getHeirId());
            if (heir != null) {
                response.setHeirName(heir.getName());
            }

            response.setExpiresAt(link.getExpiresAt());
            response.setUsedAt(link.getUsedAt());
            response.setFailCount(link.getFailCount());
            response.setCreatedAt(link.getCreatedAt());
            return response;
        }).collect(Collectors.toList());
    }

    @Override
    public void sendDeliveryOtp(DeliveryOtpRequest request) {
        DeliveryLink link = loadValidLink(request.getLinkToken());
        Heir heir = heirMapper.selectById(link.getHeirId());
        if (heir == null) {
            throw new BusinessException(ResultCode.HEIR_NOT_FOUND);
        }
        String channel = request.getChannel();
        String target;
        if ("email".equals(channel)) {
            target = heir.getEmail();
        } else if ("phone".equals(channel)) {
            target = heir.getPhone();
        } else {
            throw new BusinessException(ResultCode.PARAM_ERROR, "渠道仅支持email或phone");
        }
        if (target == null || target.trim().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "继承人缺少对应渠道联系方式");
        }

        String code = SecurityUtil.generateVerifyCode(6);
        VerificationCode vc = new VerificationCode();
        vc.setTarget(target);
        vc.setCode(code);
        vc.setCodeType("delivery_check");
        vc.setChannel(channel);
        vc.setIsUsed(0);
        vc.setExpireAt(LocalDateTime.now().plusMinutes(5));
        vc.setMockData(String.format("{\"linkId\":%d,\"heirId\":%d}", link.getId(), link.getHeirId()));
        verificationCodeMapper.insert(vc);

        if ("email".equals(channel)) {
            mockEmailService.sendVerifyCode(target, code, "delivery_check");
        } else {
            mockSmsService.sendVerifyCode(target, code);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<DeliveryContentResponse> verifyAndDecrypt(DeliveryVerifyRequest request, String ipAddress, String userAgent) {
        // 1. 校验交付链接
        DeliveryLink link = loadValidLink(request.getLinkToken());
        Heir heir = heirMapper.selectById(link.getHeirId());
        if (heir == null) {
            throw new BusinessException(ResultCode.HEIR_NOT_FOUND);
        }

        // 2. 三重身份核验
        // 2.1 邮箱OTP验证
        boolean emailVerified = verifyOtp(request.getEmailOtp(), heir.getEmail(), "email");
        if (!emailVerified) {
            recordAccessFailure(link, "identity_check", ipAddress, userAgent);
            incrementFailCount(link);
            throw new BusinessException(ResultCode.DELIVERY_IDENTITY_VERIFY_FAILED, "邮箱验证码错误");
        }

        // 2.2 手机OTP验证
        boolean phoneVerified = verifyOtp(request.getPhoneOtp(), heir.getPhone(), "phone");
        if (!phoneVerified) {
            recordAccessFailure(link, "identity_check", ipAddress, userAgent);
            incrementFailCount(link);
            throw new BusinessException(ResultCode.DELIVERY_IDENTITY_VERIFY_FAILED, "手机验证码错误");
        }

        // 2.3 人脸识别（Mock/真实服务由服务层内部开关控制）
        Map<String, Object> faceResult = mockFaceRecognitionService.verifyFace(
                link.getUserId(), link.getHeirId(), request.getFaceImageBase64());
        if (!(Boolean) faceResult.get("verified")) {
            recordAccessFailure(link, "identity_check", ipAddress, userAgent);
            incrementFailCount(link);
            throw new BusinessException(ResultCode.DELIVERY_IDENTITY_VERIFY_FAILED, "人脸核验未通过");
        }

        // 3. 仅返回分配给该继承人的内容
        List<Long> assignedContentIds = assignmentMapper.selectList(
                new LambdaQueryWrapper<HeirContentAssignment>()
                        .eq(HeirContentAssignment::getHeirId, link.getHeirId()))
                .stream()
                .map(HeirContentAssignment::getContentId)
                .collect(Collectors.toList());
        if (assignedContentIds.isEmpty()) {
            throw new BusinessException(ResultCode.CONTENT_NOT_FOUND, "当前继承人没有被分配可交付内容");
        }

        List<EncryptedContent> contents = contentMapper.selectList(
                new LambdaQueryWrapper<EncryptedContent>()
                        .eq(EncryptedContent::getUserId, link.getUserId())
                        .in(EncryptedContent::getId, assignedContentIds)
                        .eq(EncryptedContent::getStatus, Constants.CONTENT_STATUS_NORMAL));

        List<DeliveryContentResponse> responses = new ArrayList<>();
        for (EncryptedContent content : contents) {
            DeliveryContentResponse resp = new DeliveryContentResponse();
            resp.setContentId(content.getId());
            resp.setContentType(content.getContentType());

            String[] typeNames = {"私钥", "账户密码", "遗言", "文件"};
            String[] types = {Constants.CONTENT_TYPE_PRIVATE_KEY, Constants.CONTENT_TYPE_ACCOUNT_PASSWORD,
                    Constants.CONTENT_TYPE_LAST_WORDS, Constants.CONTENT_TYPE_FILE};
            for (int i = 0; i < types.length; i++) {
                if (types[i].equals(content.getContentType())) {
                    resp.setContentTypeText(typeNames[i]);
                    break;
                }
            }

            resp.setTitle(content.getTitle());
            resp.setEncryptedData(content.getEncryptedData());
            resp.setContentHash(content.getContentHash());
            resp.setK2Shard(content.getK2Shard());
            resp.setK3Shard(content.getK3Shard());
            resp.setDecryptedData(null);
            resp.setFileName(content.getFileName());
            resp.setFileSize(content.getFileSize());
            resp.setAccessedAt(LocalDateTime.now());

            // 区块链存证访问记录
            Map<String, Object> chainResult = mockBlockchainService.storeOnChain(
                    link.getUserId(), "delivery_access",
                    content.getContentHash());
            resp.setBlockchainTxHash((String) chainResult.get("txHash"));

            responses.add(resp);

            // 记录访问日志
            recordAccessSuccess(link, "decrypt", ipAddress, userAgent, request.getDeviceFingerprint());
        }

        // 4. 授权材料准备成功后，标记链接已使用
        link.setStatus(Constants.LINK_STATUS_USED);
        link.setUsedAt(LocalDateTime.now());
        deliveryLinkMapper.updateById(link);

        recordAccessSuccess(link, "identity_check", ipAddress, userAgent, request.getDeviceFingerprint());

        auditLogService.log(link.getUserId(), Constants.AUDIT_MODULE_DELIVERY, "delivery_access",
                String.format("{\"heirId\":%d,\"linkId\":%d}", link.getHeirId(), link.getId()));
        log.info("遗产交付完成 | heirId={} | 内容数={}", link.getHeirId(), responses.size());

        return responses;
    }

    /**
     * 验证OTP（简化处理，直接校验验证码表）
     */
    private DeliveryLink loadValidLink(String linkToken) {
        DeliveryLink link = deliveryLinkMapper.selectOne(
                new LambdaQueryWrapper<DeliveryLink>().eq(DeliveryLink::getLinkToken, linkToken));
        if (link == null) {
            throw new BusinessException(ResultCode.DELIVERY_LINK_INVALID);
        }
        if (link.getStatus() == Constants.LINK_STATUS_LOCKED) {
            throw new BusinessException(ResultCode.DELIVERY_LINK_LOCKED);
        }
        if (link.getStatus() == Constants.LINK_STATUS_USED) {
            throw new BusinessException(ResultCode.DELIVERY_LINK_USED);
        }
        if (link.getExpiresAt().isBefore(LocalDateTime.now())) {
            link.setStatus(Constants.LINK_STATUS_INVALID);
            deliveryLinkMapper.updateById(link);
            throw new BusinessException(ResultCode.DELIVERY_LINK_EXPIRED);
        }
        return link;
    }

    private boolean verifyOtp(String otp, String target, String channel) {
        // Mock模式：任何6位数字都通过
        if (properties.getMockModeEnabled()) {
            return otp != null && otp.length() == 6;
        }
        // 正式模式：查数据库验证
        VerificationCode vc = verificationCodeMapper.selectOne(
                new LambdaQueryWrapper<VerificationCode>()
                        .eq(VerificationCode::getTarget, target)
                        .eq(VerificationCode::getCode, otp)
                        .eq(VerificationCode::getCodeType, "delivery_check")
                        .eq(VerificationCode::getChannel, channel)
                        .eq(VerificationCode::getIsUsed, 0)
                        .gt(VerificationCode::getExpireAt, LocalDateTime.now())
                        .last("LIMIT 1"));
        if (vc != null) {
            vc.setIsUsed(1);
            verificationCodeMapper.updateById(vc);
            return true;
        }
        return false;
    }

    /**
     * 增加失败次数，超过阈值锁定链接
     */
    private void incrementFailCount(DeliveryLink link) {
        link.setFailCount(link.getFailCount() + 1);
        if (link.getFailCount() >= link.getMaxFailCount()) {
            link.setStatus(Constants.LINK_STATUS_LOCKED);
            log.warn("交付链接已锁定 | linkId={} | failCount={}", link.getId(), link.getFailCount());
        }
        deliveryLinkMapper.updateById(link);
    }

    /**
     * 记录访问失败日志
     */
    private void recordAccessFailure(DeliveryLink link, String accessType, String ipAddress, String userAgent) {
        DeliveryAccessLog accessLog = new DeliveryAccessLog();
        accessLog.setDeliveryLinkId(link.getId());
        accessLog.setHeirId(link.getHeirId());
        accessLog.setAccessType(accessType);
        accessLog.setIpAddress(ipAddress);
        accessLog.setUserAgent(userAgent);
        accessLog.setResult("FAILED");
        accessLogMapper.insert(accessLog);
    }

    /**
     * 记录访问成功日志
     */
    private void recordAccessSuccess(DeliveryLink link, String accessType, String ipAddress, String userAgent, String deviceFingerprint) {
        DeliveryAccessLog accessLog = new DeliveryAccessLog();
        accessLog.setDeliveryLinkId(link.getId());
        accessLog.setHeirId(link.getHeirId());
        accessLog.setAccessType(accessType);
        accessLog.setIpAddress(ipAddress);
        accessLog.setUserAgent(userAgent);
        accessLog.setDeviceFingerprint(deviceFingerprint);
        accessLog.setResult("SUCCESS");

        // 区块链存证
        Map<String, Object> chainResult = mockBlockchainService.storeOnChain(
                link.getUserId(), "delivery_access_log",
                String.valueOf(link.getId()));
        accessLog.setBlockchainTxHash((String) chainResult.get("txHash"));

        accessLogMapper.insert(accessLog);
    }
}
