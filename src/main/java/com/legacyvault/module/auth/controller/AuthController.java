package com.legacyvault.module.auth.controller;

import com.legacyvault.common.Result;
import com.legacyvault.module.auth.dto.*;
import com.legacyvault.module.auth.service.AuthService;
import com.legacyvault.util.RequestUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

/**
 * 认证控制器
 * 处理注册、登录、验证码、TOTP绑定、恢复码等接口
 *
 * @author LegacyVault
 */
@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 发送验证码
     * POST /api/auth/send-code
     */
    @PostMapping("/send-code")
    public Result<String> sendVerifyCode(@Valid @RequestBody SendCodeRequest request) {
        authService.sendVerifyCode(request);
        return Result.success("验证码已发送");
    }

    /**
     * 用户注册
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public Result<String> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return Result.success("注册成功");
    }

    /**
     * 用户登录（账号密码）
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String ip = RequestUtil.getIpAddress(httpRequest);
        String ua = RequestUtil.getUserAgent(httpRequest);
        LoginResponse response = authService.login(request, ip, ua);
        return Result.success("登录成功", response);
    }

    /**
     * 验证码登录（手机号/邮箱 + 验证码）
     * POST /api/auth/login/code
     */
    @PostMapping("/login/code")
    public Result<LoginResponse> loginByCode(@Valid @RequestBody CodeLoginRequest request, HttpServletRequest httpRequest) {
        String ip = RequestUtil.getIpAddress(httpRequest);
        String ua = RequestUtil.getUserAgent(httpRequest);
        LoginResponse response = authService.loginByCode(request, ip, ua);
        return Result.success("登录成功", response);
    }

    /**
     * 用户登出
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest httpRequest) {
        Long userId = RequestUtil.getCurrentUserId(httpRequest);
        authService.logout(userId);
        return Result.success("登出成功");
    }

    /**
     * 初始化TOTP绑定（获取密钥和二维码）
     * POST /api/auth/totp/init
     */
    @PostMapping("/totp/init")
    public Result<TotpBindResponse> initTotpBind(HttpServletRequest httpRequest) {
        Long userId = RequestUtil.getCurrentUserId(httpRequest);
        TotpBindResponse response = authService.initTotpBind(userId);
        return Result.success(response);
    }

    /**
     * 确认TOTP绑定
     * POST /api/auth/totp/confirm
     */
    @PostMapping("/totp/confirm")
    public Result<String> confirmTotpBind(@Valid @RequestBody TotpVerifyRequest request, HttpServletRequest httpRequest) {
        Long userId = RequestUtil.getCurrentUserId(httpRequest);
        authService.confirmTotpBind(userId, request.getTotpCode());
        return Result.success("TOTP绑定成功");
    }

    /**
     * 生成紧急恢复码
     * POST /api/auth/recovery-codes/generate
     */
    @PostMapping("/recovery-codes/generate")
    public Result<List<String>> generateRecoveryCodes(HttpServletRequest httpRequest) {
        Long userId = RequestUtil.getCurrentUserId(httpRequest);
        List<String> codes = authService.generateRecoveryCodes(userId);
        return Result.success("恢复码已生成，请妥善保管（仅展示一次）", codes);
    }

    /**
     * 使用恢复码中止触发流程
     * POST /api/auth/recovery-codes/use
     */
    @PostMapping("/recovery-codes/use")
    public Result<String> useRecoveryCode(@RequestParam String code, HttpServletRequest httpRequest) {
        Long userId = RequestUtil.getCurrentUserId(httpRequest);
        authService.useRecoveryCode(userId, code);
        return Result.success("恢复码已使用，触发流程已中止");
    }

    /**
     * 绑定邮箱或手机号，补齐 PRD 要求的邮箱 + 手机号身份锚定。
     * POST /api/auth/bind-contact
     */
    @PostMapping("/bind-contact")
    public Result<String> bindContact(@Valid @RequestBody BindContactRequest request, HttpServletRequest httpRequest) {
        Long userId = RequestUtil.getCurrentUserId(httpRequest);
        authService.bindContact(userId, request);
        return Result.success("联系方式已绑定并验证");
    }
}
