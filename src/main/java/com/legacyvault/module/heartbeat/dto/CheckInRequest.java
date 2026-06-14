package com.legacyvault.module.heartbeat.dto;

import lombok.Data;
import javax.validation.constraints.*;

/**
 * 心跳打卡请求DTO
 */
@Data
public class CheckInRequest {
    @NotBlank(message = "TOTP验证码不能为空")
    private String totpCode;
}
