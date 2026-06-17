package com.legacyvault.module.admin.dto;

import lombok.Data;

/**
 * 管理员登录响应
 *
 * @author LegacyVault
 */
@Data
public class AdminLoginResponse {

    private String token;
    private Long adminId;
    private String username;
    private String realName;
    private String role;
}
