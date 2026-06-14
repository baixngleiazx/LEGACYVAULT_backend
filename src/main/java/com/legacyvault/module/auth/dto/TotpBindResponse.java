package com.legacyvault.module.auth.dto;

import lombok.Data;

/**
 * TOTP绑定响应VO（包含密钥和二维码URI）
 */
@Data
public class TotpBindResponse {
    private String secret;
    private String otpAuthUri;
    private String qrCodeUrl;
}
