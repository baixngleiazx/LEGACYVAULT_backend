package com.legacyvault.module.kyc.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * KYC 状态响应 DTO
 *
 * @author LegacyVault
 */
@Data
public class KycStatusResponse {

    /** 当前状态：0未提交 1机审通过 2机审失败 3待人工审核 4人工通过 5人工驳回 */
    private Integer status;

    /** 状态文案 */
    private String statusText;

    /** 驳回原因（仅驳回状态有值） */
    private String rejectReason;

    /** 服务商 */
    private String provider;

    /** 最新单据 ID */
    private Long recordId;

    /** 提交时间 */
    private LocalDateTime submittedAt;

    /** 审核时间 */
    private LocalDateTime reviewedAt;
}
