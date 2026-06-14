package com.legacyvault.module.auth.dto;

import lombok.Data;

/**
 * 登录响应VO
 */
@Data
public class LoginResponse {
    private String token;
    private Long userId;
    private String email;
    private String phone;
    private String nickname;
    private Integer totpBound;
    private Integer securityScore;
    private Long planId;
    private String planName;
}
