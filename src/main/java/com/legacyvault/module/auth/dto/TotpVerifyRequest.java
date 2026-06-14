package com.legacyvault.module.auth.dto;

import lombok.Data;
import javax.validation.constraints.*;

/**
 * TOTP验证请求DTO
 */
@Data
public class TotpVerifyRequest {
    @NotBlank(message = "TOTP验证码不能为空")
    @Size(min = 6, max = 6, message = "TOTP验证码为6位数字")
    private String totpCode;
}
