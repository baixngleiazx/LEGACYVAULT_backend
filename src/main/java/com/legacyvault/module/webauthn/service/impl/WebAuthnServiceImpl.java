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

import java.time.LocalDateTime;
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

    @Override
    public WebAuthnInitResponse initRegistration(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 生成挑战值
        String challenge = SecurityUtil.generateToken() + SecurityUtil.generateToken();
        String redisKey = Constants.REDIS_WEBAUTHN_CHALLENGE_PREFIX + userId;
        redisTemplate.opsForValue().set(redisKey, challenge, CHALLENGE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        WebAuthnInitResponse resp = new WebAuthnInitResponse();
        resp.setChallenge(challenge);
        resp.setRpId("legacy-vault.local");
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

        // Mock 模式：直接接受任何响应；真实模式需调用 WebAuthnManager.finishRegistration 校验
        if (!properties.getMockModeEnabled()) {
            log.warn("真实 WebAuthn 校验暂未接入，请使用 Mock 模式");
        }

        // 持久化凭证
        WebAuthnCredential credential = new WebAuthnCredential();
        credential.setUserId(userId);
        credential.setCredentialId(request.getCredentialId());
        credential.setPublicKey(request.getPublicKey());
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
}
