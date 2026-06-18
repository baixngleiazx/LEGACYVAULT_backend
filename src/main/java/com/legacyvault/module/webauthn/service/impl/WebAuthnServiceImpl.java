package com.legacyvault.module.webauthn.service.impl;

import com.legacyvault.common.Constants;
import com.legacyvault.common.ResultCode;
import com.legacyvault.config.LegacyVaultProperties;
import com.legacyvault.exception.BusinessException;
import com.legacyvault.module.auth.entity.TotpConfig;
import com.legacyvault.module.auth.mapper.TotpConfigMapper;
import com.legacyvault.module.auth.service.AuditLogService;
import com.legacyvault.module.user.entity.User;
import com.legacyvault.module.user.mapper.UserMapper;
import com.legacyvault.module.webauthn.dto.WebAuthnConfirmRequest;
import com.legacyvault.module.webauthn.dto.WebAuthnInitResponse;
import com.legacyvault.module.webauthn.entity.WebAuthnCredential;
import com.legacyvault.module.webauthn.mapper.WebAuthnCredentialMapper;
import com.legacyvault.module.webauthn.service.WebAuthnService;
import com.legacyvault.util.SecurityUtil;
import com.legacyvault.util.TotpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * WebAuthn 服务实现
 * Mock 模式简化处理：直接生成挑战并接受任意响应；真实模式预留 java-webauthn-server SDK 接入点
 *
 * @author LegacyVault
 */
@Slf4j
@Service
public class WebAuthnServiceImpl implements WebAuthnService {

    private static final long CHALLENGE_EXPIRE_MINUTES = 5;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Autowired
    private WebAuthnCredentialMapper webAuthnCredentialMapper;

    @Autowired
    private TotpConfigMapper totpConfigMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private LegacyVaultProperties properties;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public WebAuthnInitResponse initRegistration(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        byte[] challengeBytes = new byte[32];
        SECURE_RANDOM.nextBytes(challengeBytes);
        String challenge = Base64.getUrlEncoder().withoutPadding().encodeToString(challengeBytes);
        String redisKey = Constants.REDIS_WEBAUTHN_CHALLENGE_PREFIX + userId;
        redisTemplate.opsForValue().set(redisKey, challenge, CHALLENGE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        WebAuthnInitResponse resp = new WebAuthnInitResponse();
        resp.setChallenge(challenge);
        resp.setRpId("localhost");
        resp.setRpName(properties.getTotp().getIssuer());
        resp.setUserId(String.valueOf(userId));
        resp.setUserName(user.getEmail() != null ? user.getEmail() : user.getPhone());
        resp.setTimeout(CHALLENGE_EXPIRE_MINUTES * 60 * 1000L);

        log.info("WebAuthn 注册初始化 | userId={}", userId);

        /*
         * ========== 真实模式预留 ==========
         * 正式接入 java-webauthn-server：
         *   ByteArray challenge = ByteArray.fromBase64UrlString(...);
         *   PublicKeyCredentialCreationOptions options = factory.createRegistrationOptions(...);
         * 当前 Mock 模式直接返回固定结构，前端可自由构造响应
         */
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmRegistration(Long userId, WebAuthnConfirmRequest request) {
        // 校验挑战是否存在
        String redisKey = Constants.REDIS_WEBAUTHN_CHALLENGE_PREFIX + userId;
        Object challenge = redisTemplate.opsForValue().get(redisKey);
        if (challenge == null) {
            throw new BusinessException("WebAuthn 挑战已过期，请重新初始化");
        }

        // Mock 模式兼容自动化自测；真实模式至少校验浏览器 WebAuthn clientData。
        if (!properties.getMockModeEnabled()) {
            validateClientData((String) challenge, request);
        }

        // 持久化凭证
        WebAuthnCredential credential = new WebAuthnCredential();
        credential.setUserId(userId);
        credential.setCredentialId(request.getCredentialId());
        credential.setPublicKey(resolvePublicKeyMaterial(request));
        credential.setSignCount(0L);
        credential.setDeviceName(request.getDeviceName() != null ? request.getDeviceName() : "YubiKey");
        credential.setBoundAt(LocalDateTime.now());
        webAuthnCredentialMapper.insert(credential);

        // 同时写 TotpConfig（deviceType=hardware）以便登录流程统一校验
        String totpSecret = TotpUtil.generateSecret();
        TotpConfig totpConfig = new TotpConfig();
        totpConfig.setUserId(userId);
        totpConfig.setSecretKey(totpSecret);
        totpConfig.setIssuer(properties.getTotp().getIssuer());
        totpConfig.setDeviceType("hardware");
        totpConfig.setBoundAt(LocalDateTime.now());
        totpConfigMapper.insert(totpConfig);

        // 更新用户状态
        User user = userMapper.selectById(userId);
        user.setTotpBound(Constants.TOTP_BOUND);
        user.setSecurityScore(Math.min(100, (user.getSecurityScore() != null ? user.getSecurityScore() : 0) + 20));
        userMapper.updateById(user);

        // 删除挑战
        redisTemplate.delete(redisKey);

        auditLogService.log(userId, Constants.AUDIT_MODULE_AUTH, "bind_webauthn",
                String.format("{\"device\":\"%s\"}", credential.getDeviceName()));
        log.info("WebAuthn 凭证绑定成功 | userId={} | device={}", userId, credential.getDeviceName());
    }

    private void validateClientData(String expectedChallenge, WebAuthnConfirmRequest request) {
        try {
            String clientDataJson = new String(Base64.getUrlDecoder().decode(request.getClientDataJSON()), StandardCharsets.UTF_8);
            JsonNode clientData = objectMapper.readTree(clientDataJson);
            if (!"webauthn.create".equals(clientData.path("type").asText())) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "WebAuthn类型不正确");
            }
            if (!expectedChallenge.equals(clientData.path("challenge").asText())) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "WebAuthn挑战值不匹配");
            }
            String origin = clientData.path("origin").asText();
            if (origin == null || origin.isEmpty()) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "WebAuthn origin缺失");
            }
            if (request.getAttestationObject() == null || request.getAttestationObject().isEmpty()) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "WebAuthn attestationObject缺失");
            }
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "WebAuthn数据不是有效Base64Url");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "WebAuthn客户端数据解析失败");
        }
    }

    private String resolvePublicKeyMaterial(WebAuthnConfirmRequest request) {
        if (request.getPublicKey() != null && !request.getPublicKey().isEmpty()) {
            return request.getPublicKey();
        }
        return request.getAttestationObject();
    }
}
