package com.legacyvault.module.delivery.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.legacyvault.common.Constants;
import com.legacyvault.common.ResultCode;
import com.legacyvault.config.LegacyVaultProperties;
import com.legacyvault.exception.BusinessException;
import com.legacyvault.mock.MockBlockchainService;
import com.legacyvault.mock.MockFaceRecognitionService;
import com.legacyvault.module.auth.entity.VerificationCode;
import com.legacyvault.module.auth.mapper.VerificationCodeMapper;
import com.legacyvault.module.auth.service.AuditLogService;
import com.legacyvault.module.content.entity.EncryptedContent;
import com.legacyvault.module.content.mapper.EncryptedContentMapper;
import com.legacyvault.module.delivery.dto.DeliveryContentResponse;
import com.legacyvault.module.delivery.dto.DeliveryLinkResponse;
import com.legacyvault.module.delivery.dto.DeliveryVerifyRequest;
import com.legacyvault.module.delivery.entity.DeliveryAccessLog;
import com.legacyvault.module.delivery.entity.DeliveryLink;
import com.legacyvault.module.delivery.mapper.DeliveryAccessLogMapper;
import com.legacyvault.module.delivery.mapper.DeliveryLinkMapper;
import com.legacyvault.module.delivery.service.DeliveryService;
import com.legacyvault.module.user.entity.Heir;
import com.legacyvault.module.user.mapper.HeirMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private VerificationCodeMapper verificationCodeMapper;

    @Autowired
    private MockFaceRecognitionService mockFaceRecognitionService;

    @Autowired
    private MockBlockchainService mockBlockchainService;

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
    @Transactional(rollbackFor = Exception.class)
    public List<DeliveryContentResponse> verifyAndDecrypt(DeliveryVerifyRequest request, String ipAddress, String userAgent) {
        // 1. 校验交付链接
        DeliveryLink link = deliveryLinkMapper.selectOne(
                new LambdaQueryWrapper<DeliveryLink>().eq(DeliveryLink::getLinkToken, request.getLinkToken()));
        if (link == null) {
            throw new BusinessException(ResultCode.DELIVERY_LINK_INVALID);
        }

        // 检查链接状态
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

        // 2. 三重身份核验
        // 2.1 邮箱OTP验证
        boolean emailVerified = verifyOtp(request.getEmailOtp(), link.getHeirId(), "email");
        if (!emailVerified) {
            recordAccessFailure(link, "identity_check", ipAddress, userAgent);
            incrementFailCount(link);
            throw new BusinessException(ResultCode.DELIVERY_IDENTITY_VERIFY_FAILED, "邮箱验证码错误");
        }

        // 2.2 手机OTP验证
        boolean phoneVerified = verifyOtp(request.getPhoneOtp(), link.getHeirId(), "phone");
        if (!phoneVerified) {
            recordAccessFailure(link, "identity_check", ipAddress, userAgent);
            incrementFailCount(link);
            throw new BusinessException(ResultCode.DELIVERY_IDENTITY_VERIFY_FAILED, "手机验证码错误");
        }

        // 2.3 人脸识别（Mock）
        if (request.getFaceImageBase64() != null) {
            Heir heir = heirMapper.selectById(link.getHeirId());
            Map<String, Object> faceResult = mockFaceRecognitionService.verifyFace(
                    link.getUserId(), link.getHeirId(), request.getFaceImageBase64());
            if (!(Boolean) faceResult.get("verified")) {
                recordAccessFailure(link, "identity_check", ipAddress, userAgent);
                incrementFailCount(link);
                throw new BusinessException(ResultCode.DELIVERY_IDENTITY_VERIFY_FAILED, "人脸核验未通过");
            }
        }

        // 3. 核验通过，标记链接已使用
        link.setStatus(Constants.LINK_STATUS_USED);
        link.setUsedAt(LocalDateTime.now());
        deliveryLinkMapper.updateById(link);

        // 记录成功访问
        recordAccessSuccess(link, "identity_check", ipAddress, userAgent, request.getDeviceFingerprint());

        // 4. 获取用户的所有加密内容并返回（模拟解密）
        List<EncryptedContent> contents = contentMapper.selectList(
                new LambdaQueryWrapper<EncryptedContent>()
                        .eq(EncryptedContent::getUserId, link.getUserId())
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
            // 模拟解密：实际应由浏览器WebCrypto在本地完成
            // 这里直接返回密文，前端需用Shamir重组密钥后本地解密
            resp.setDecryptedData("[需浏览器本地WebCrypto解密] " + content.getEncryptedData().substring(0, Math.min(50, content.getEncryptedData().length())) + "...");
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

        auditLogService.log(link.getUserId(), Constants.AUDIT_MODULE_DELIVERY, "delivery_access",
                String.format("{\"heirId\":%d,\"linkId\":%d}", link.getHeirId(), link.getId()));
        log.info("遗产交付完成 | heirId={} | 内容数={}", link.getHeirId(), responses.size());

        return responses;
    }

    /**
     * 验证OTP（简化处理，直接校验验证码表）
     */
    private boolean verifyOtp(String otp, Long heirId, String channel) {
        // Mock模式：任何6位数字都通过
        if (properties.getMockModeEnabled()) {
            return otp != null && otp.length() == 6;
        }
        // 正式模式：查数据库验证
        VerificationCode vc = verificationCodeMapper.selectOne(
                new LambdaQueryWrapper<VerificationCode>()
                        .eq(VerificationCode::getCode, otp)
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
