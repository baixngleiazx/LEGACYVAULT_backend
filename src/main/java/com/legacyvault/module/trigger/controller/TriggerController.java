package com.legacyvault.module.trigger.controller;

import com.legacyvault.common.Result;
import com.legacyvault.module.trigger.dto.ContactReplyRequest;
import com.legacyvault.module.trigger.dto.TriggerProcessResponse;
import com.legacyvault.module.trigger.service.TriggerService;
import com.legacyvault.util.RequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

/**
 * 触发验证引擎控制器
 *
 * @author LegacyVault
 */
@RestController
@RequestMapping("/trigger")
public class TriggerController {

    @Autowired
    private TriggerService triggerService;

    /**
     * 获取触发流程列表
     * GET /api/trigger/list
     */
    @GetMapping("/list")
    public Result<List<TriggerProcessResponse>> listProcesses(HttpServletRequest request) {
        Long userId = RequestUtil.getCurrentUserId(request);
        return Result.success(triggerService.listProcesses(userId));
    }

    /**
     * 获取最新触发流程
     * GET /api/trigger/latest
     */
    @GetMapping("/latest")
    public Result<TriggerProcessResponse> getLatestProcess(HttpServletRequest request) {
        Long userId = RequestUtil.getCurrentUserId(request);
        TriggerProcessResponse process = triggerService.getLatestProcess(userId);
        return Result.success(process);
    }

    /**
     * 中止触发流程（密码+TOTP）
     * POST /api/trigger/abort
     */
    @PostMapping("/abort")
    public Result<String> abortProcess(@RequestParam String totpCode, HttpServletRequest request) {
        Long userId = RequestUtil.getCurrentUserId(request);
        triggerService.abortProcess(userId, totpCode);
        return Result.success("触发流程已中止");
    }

    /**
     * 可信联系人核查回复（无需登录，通过链接访问）
     * POST /api/trigger/contact-reply
     */
    @PostMapping("/contact-reply")
    public Result<String> contactReply(@Valid @RequestBody ContactReplyRequest request) {
        triggerService.replyContactVerification(request);
        return Result.success("核查回复已提交");
    }
}
