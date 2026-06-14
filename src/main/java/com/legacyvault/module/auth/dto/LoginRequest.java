package com.legacyvault.module.auth.dto;

import lombok.Data;

/**
 * 用户登录请求DTO
 * 支持邮箱+密码登录 和 手机号+密码登录
 * 字段 email 同时兼容邮箱或手机号输入（保持前端调用兼容）
 */
@Data
public class LoginRequest {

    /**
     * 登录账号：邮箱 或 手机号（兼容字段，保持前端调用不变）
     * - 包含 @ 按邮箱处理
     * - 符合手机号格式按手机号处理
     * 注：非空校验在 AuthService 中手动完成，以返回更精确的双语错误提示
     */
    private String email;

    /** 密码 */
    private String password;

    /** TOTP验证码（绑定TOTP后必填） */
    private String totpCode;
}
