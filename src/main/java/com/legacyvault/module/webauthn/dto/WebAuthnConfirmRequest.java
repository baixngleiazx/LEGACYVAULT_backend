package com.legacyvault.module.webauthn.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * WebAuthn 确认请求（前端完成硬件密钥注册后回传凭证）
 *
 * @author LegacyVault
 */
@Data
public class WebAuthnConfirmRequest {

    /** 凭证 ID（Base64） */
    @NotBlank(message = "凭证ID不能为空")
    private String credentialId;

    /** 客户端数据 JSON（Base64） */
    @NotBlank(message = "客户端数据不能为空")
    private String clientDataJSON;

    /** 认证器数据 JSON（Base64） */
    @NotBlank(message = "认证器数据不能为空")
    private String authenticatorData;

    /** attestationObject（Base64Url） */
    private String attestationObject;

    /** 签名（Base64，Mock 模式下可忽略） */
    private String signature;

    /** 公钥或 attestation 原始材料（Base64Url，部分浏览器可通过 getPublicKey 获取） */
    private String publicKey;

    /** 浏览器 origin */
    private String origin;

    /** 设备名称（如 "My YubiKey 5"） */
    private String deviceName;
}
