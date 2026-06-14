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
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}
