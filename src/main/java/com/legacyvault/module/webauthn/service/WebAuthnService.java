package com.legacyvault.module.webauthn.service;

import com.legacyvault.module.webauthn.dto.WebAuthnConfirmRequest;
import com.legacyvault.module.webauthn.dto.WebAuthnInitResponse;

/**
 * WebAuthn 服务接口（YubiKey 硬件密钥）
 *
 * @author LegacyVault
 */
public interface WebAuthnService {

    /**
     * 初始化 WebAuthn 注册流程（下发挑战）
     */
    WebAuthnInitResponse initRegistration(Long userId);

    /**
     * 确认 WebAuthn 注册（校验并持久化凭证）
     */
    void confirmRegistration(Long userId, WebAuthnConfirmRequest request);
}
