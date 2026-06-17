package com.legacyvault.module.biometric.controller;

import com.legacyvault.common.Result;
import com.legacyvault.module.biometric.dto.BiometricRegisterRequest;
import com.legacyvault.module.biometric.dto.BiometricStatusResponse;
import com.legacyvault.module.biometric.service.BiometricService;
import com.legacyvault.util.RequestUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 生物特征控制器
 * 处理人脸 / 指纹录入与状态查询
 *
 * @author LegacyVault
 */
@Slf4j
@RestController
@RequestMapping("/auth/biometric")
public class BiometricController {

    @Autowired
    private BiometricService biometricService;

    /**
     * 录入生物特征标识
     * POST /api/auth/biometric/register
     */
    @PostMapping("/register")
    public Result<String> register(@Valid @RequestBody BiometricRegisterRequest request,
                                   HttpServletRequest httpRequest) {
        Long userId = RequestUtil.getCurrentUserId(httpRequest);
        biometricService.register(userId, request);
        return Result.success("生物特征录入成功");
    }

    /**
     * 查询生物特征绑定状态
     * GET /api/auth/biometric/status
     */
    @GetMapping("/status")
    public Result<BiometricStatusResponse> getStatus(HttpServletRequest request) {
        Long userId = RequestUtil.getCurrentUserId(request);
        return Result.success(biometricService.getStatus(userId));
    }
}
