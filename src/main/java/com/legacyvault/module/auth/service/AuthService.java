package com.legacyvault.module.auth.service;

import com.legacyvault.module.auth.dto.*;

import java.util.List;

/**
 * 认证服务接口
 * 处理注册、登录、验证码、TOTP、恢复码等认证相关逻辑
 *
 * @author LegacyVault
 */
public interface AuthService {

    /**
     * 发送验证码
     */
    void sendVerifyCode(SendCodeRequest request);

    /**
     * 用户注册
     */
    void register(RegisterRequest request);

    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request, String ipAddress, String userAgent);

    /**
     * 验证码登录（手机号/邮箱 + 验证码）
     */
    LoginResponse loginByCode(CodeLoginRequest request, String ipAddress, String userAgent);

    /**
     * 用户登出
     */
    void logout(Long userId);

    /**
     * 初始化TOTP绑定（生成密钥和二维码URI）
     */
    TotpBindResponse initTotpBind(Long userId);

    /**
     * 确认TOTP绑定（验证用户输入的TOTP码）
     */
    void confirmTotpBind(Long userId, String totpCode);

    /**
     * 生成紧急恢复码
     */
    List<String> generateRecoveryCodes(Long userId);

    /**
     * 使用恢复码中止触发流程
     */
    void useRecoveryCode(Long userId, String recoveryCode);
}
