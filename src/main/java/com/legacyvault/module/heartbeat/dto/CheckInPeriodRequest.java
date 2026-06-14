package com.legacyvault.module.heartbeat.dto;

import lombok.Data;
import javax.validation.constraints.*;

/**
 * 设置打卡周期请求DTO
 */
@Data
public class CheckInPeriodRequest {
    @NotNull(message = "打卡周期不能为空")
    @Min(value = 30, message = "最短30天")
    @Max(value = 180, message = "最长180天")
    private Integer periodDays;
}
