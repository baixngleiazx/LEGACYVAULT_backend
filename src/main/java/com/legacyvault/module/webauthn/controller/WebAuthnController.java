package com.legacyvault.module.webauthn.controller;

import com.legacyvault.common.Result;
import com.legacyvault.module.webauthn.dto.WebAuthnConfirmRequest;
import com.legacyvault.module.webauthn.dto.WebAuthnInitResponse;
import com.legacyvault.module.webauthn.service.WebAuthnService;
import com.legacyvault.util.RequestUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * WebAuthn 控制器（YubiKey 硬件密钥）
 *
 * @author LegacyVault
 */
@Slf4j
@RestController
@RequestMapping("/auth/totp/webauthn")
public class WebAuthnController {

    @Autowired
    private WebAuthnService webAuthnService;

    /**
     * 初始化 WebAuthn 注册流程（下发挑战）
     * POST /api/auth/totp/webauthn/init
     */
    @PostMapping("/init")
    public Result<WebAuthnInitResponse> init(HttpServletRequest request) {
        Long userId = RequestUtil.getCurrentUserId(request);
        return Result.success(webAuthnService.initRegistration(userId));
    }

    /**
     * 确认 WebAuthn 注册（回传凭证）
     * POST /api/auth/totp/webauthn/confirm
     */
    @PostMapping("/confirm")
    public Result<String> confirm(@Valid @RequestBody WebAuthnConfirmRequest request,
                                  HttpServletRequest httpRequest) {
        Long userId = RequestUtil.getCurrentUserId(httpRequest);
        webAuthnService.confirmRegistration(userId, request);
        return Result.success("硬件密钥绑定成功");
    }
}
