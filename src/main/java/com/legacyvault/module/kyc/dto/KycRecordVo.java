package com.legacyvault.module.kyc.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * KYC 单据 VO（管理员查询用，敏感字段脱敏）
 *
 * @author LegacyVault
 */
@Data
public class KycRecordVo {

    private Long id;
    private Long userId;
    private String userEmail;
    private String userPhone;
    private String userNickname;
    private String realName;
    private String idType;
    /** 证件号脱敏后展示 */
    private String idNoMasked;
    private Integer livenessPassed;
    private Integer status;
    private String statusText;
    private String provider;
    private String rejectReason;
    private Long reviewerId;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
}
