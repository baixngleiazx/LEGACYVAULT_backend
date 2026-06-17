package com.legacyvault.module.user.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户信息响应VO
 */
@Data
public class UserInfoResponse {
    private Long id;
    private String email;
    private String phone;
    private String nickname;
    private Integer status;
    private Integer totpBound;
    private Integer biometricBound;
    private Integer kycStatus;
    private Integer securityScore;
    private Integer travelModeEnabled;
    private LocalDateTime travelStartDate;
    private LocalDateTime travelEndDate;
    private Long planId;
    private String planName;

    // ===== 5 步注册流程状态 =====
    private Integer step1Done;
    private Integer step2Done;
    private Integer step3Done;
    private Integer step4Done;
    private Integer step5Done;
    private Integer step1Skipped;
    private Integer step2Skipped;
    private Integer step3Skipped;
    private Integer step4Skipped;
    private Integer step5Skipped;

    // ===== 继承人门槛 =====
    private Integer planHeirLimit;
    private Integer minHeirsToUnlock;
    private java.math.BigDecimal kycAssetThreshold;
    private String kycRejectReason;

    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}
