package com.legacyvault.module.registration.controller;

import com.legacyvault.common.Result;
import com.legacyvault.module.registration.dto.RegistrationStatusResponse;
import com.legacyvault.module.registration.service.RegistrationService;
import com.legacyvault.util.RequestUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 5 步注册流程控制器
 * 每一步都支持「完成」与「跳过」两种终结方式
 *
 * @author LegacyVault
 */
@Slf4j
@RestController
@RequestMapping("/auth/registration")
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;

    /**
     * 查询 5 步注册流程整体状态
     * GET /api/auth/registration/status
     */
    @GetMapping("/status")
    public Result<RegistrationStatusResponse> getStatus(HttpServletRequest request) {
        Long userId = RequestUtil.getCurrentUserId(request);
        return Result.success(registrationService.getStatus(userId));
    }

    /**
     * 标记某一步骤完成
     * POST /api/auth/registration/step/{step}/complete
     */
    @PostMapping("/step/{step}/complete")
    public Result<String> completeStep(@PathVariable int step, HttpServletRequest request) {
        Long userId = RequestUtil.getCurrentUserId(request);
        registrationService.completeStep(userId, step);
        return Result.success("步骤 " + step + " 已完成");
    }

    /**
     * 标记某一步骤跳过
     * POST /api/auth/registration/step/{step}/skip
     */
    @PostMapping("/step/{step}/skip")
    public Result<String> skipStep(@PathVariable int step, HttpServletRequest request) {
        Long userId = RequestUtil.getCurrentUserId(request);
        registrationService.skipStep(userId, step);
        return Result.success("步骤 " + step + " 已跳过");
    }

    /**
     * 标记整个注册流程完成（触发后置动作）
     * POST /api/auth/registration/complete
     */
    @PostMapping("/complete")
    public Result<String> completeAll(HttpServletRequest request) {
        Long userId = RequestUtil.getCurrentUserId(request);
        registrationService.completeAll(userId);
        return Result.success("注册流程已全部完成");
    }
}
