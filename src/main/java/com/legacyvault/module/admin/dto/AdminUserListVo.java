package com.legacyvault.module.admin.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 管理员视角下的用户列表 VO（敏感字段脱敏）
 *
 * @author LegacyVault
 */
@Data
public class AdminUserListVo {

    private Long id;
    private String email;
    private String phone;
    private String nickname;
    private Integer status;
    private Long planId;
    private String planName;
    private Integer securityScore;
    private Integer totpBound;
    private Integer biometricBound;
    private Integer kycStatus;
    private String kycStatusText;
    private String kycRejectReason;
    private Integer step1Done;
    private Integer step2Done;
    private Integer step3Done;
    private Integer step4Done;
    private Integer step5Done;
    private Integer heirCount;
    private BigDecimal kycAssetThreshold;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}
