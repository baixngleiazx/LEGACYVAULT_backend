package com.legacyvault.module.heartbeat.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 心跳状态响应VO
 */
@Data
public class HeartbeatStatusResponse {
    private Integer checkInPeriodDays;
    private LocalDateTime nextDeadline;
    private LocalDateTime lastCheckInAt;
    private Integer daysRemaining;
    private String statusColor;  // green/yellow/red
    private Boolean travelModeEnabled;
    private LocalDateTime travelStartDate;
    private LocalDateTime travelEndDate;
}
