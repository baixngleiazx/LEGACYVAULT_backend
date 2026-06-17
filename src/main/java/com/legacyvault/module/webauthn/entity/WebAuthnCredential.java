package com.legacyvault.module.webauthn.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * WebAuthn 凭证实体（YubiKey 硬件密钥）
 * 对应表：webauthn_credential
 *
 * @author LegacyVault
 */
@Data
@TableName("webauthn_credential")
public class WebAuthnCredential implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户ID */
    private Long userId;

    /** 凭证ID */
    private String credentialId;

    /** 公钥 */
    private String publicKey;

    /** 签名计数 */
    private Long signCount;

    /** 设备名称 */
    private String deviceName;

    /** 绑定时间 */
    private LocalDateTime boundAt;
}
