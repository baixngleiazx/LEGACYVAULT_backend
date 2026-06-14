package com.legacyvault.module.heartbeat.dto;

import lombok.Data;
import javax.validation.constraints.*;

/**
 * 旅行模式设置请求DTO
 */
@Data
public class TravelModeRequest {
    @NotNull(message = "开始日期不能为空")
    private String startDate;

    @NotNull(message = "结束日期不能为空")
    private String endDate;

    @NotBlank(message = "TOTP验证码不能为空")
    private String totpCode;
}
