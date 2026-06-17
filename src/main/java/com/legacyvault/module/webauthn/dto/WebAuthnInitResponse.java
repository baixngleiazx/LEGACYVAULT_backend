package com.legacyvault.module.webauthn.dto;

import lombok.Data;

/**
 * WebAuthn 初始化响应（下发挑战给前端）
 *
 * @author LegacyVault
 */
@Data
public class WebAuthnInitResponse {

    /** 挑战值（Base64） */
    private String challenge;

    /** Relying Party ID */
    private String rpId;

    /** Relying Party 名称 */
    private String rpName;

    /** 用户 ID（Base64） */
    private String userId;

    /** 用户名称 */
    private String userName;

    /** 超时时间（毫秒） */
    private Long timeout;
}
