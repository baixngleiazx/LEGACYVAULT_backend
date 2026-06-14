package com.legacyvault.module.trigger.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 触发流程响应VO
 */
@Data
public class TriggerProcessResponse {
    private Long id;
    private String status;
    private String statusText;
    private LocalDateTime gracePeriodStart;
    private LocalDateTime contactCheckStart;
    private LocalDateTime notaryNotifyAt;
    private LocalDateTime finalConfirmAt;
    private LocalDateTime completedAt;
    private LocalDateTime abortedAt;
    private String abortReason;
    private String blockchainTxHash;
    private LocalDateTime createdAt;
}
