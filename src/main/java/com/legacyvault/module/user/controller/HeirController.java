package com.legacyvault.module.user.controller;

import com.legacyvault.common.Result;
import com.legacyvault.module.user.dto.*;
import com.legacyvault.module.user.service.HeirService;
import com.legacyvault.util.RequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

/**
 * 继承人管理控制器
 *
 * @author LegacyVault
 */
@RestController
@RequestMapping("/heir")
public class HeirController {

    @Autowired
    private HeirService heirService;

    /**
     * 获取继承人列表
     * GET /api/heir/list
     */
    @GetMapping("/list")
    public Result<List<HeirResponse>> listHeirs(HttpServletRequest request) {
        Long userId = RequestUtil.getCurrentUserId(request);
        return Result.success(heirService.listHeirs(userId));
    }

    /**
     * 添加继承人
     * POST /api/heir/add
     */
    @PostMapping("/add")
    public Result<HeirResponse> addHeir(@Valid @RequestBody HeirRequest request, HttpServletRequest httpRequest) {
        Long userId = RequestUtil.getCurrentUserId(httpRequest);
        return Result.success("继承人已添加，确认邀请已发送", heirService.addHeir(userId, request));
    }

    /**
     * 编辑继承人
     * PUT /api/heir/{heirId}
     */
    @PutMapping("/{heirId}")
    public Result<HeirResponse> updateHeir(@PathVariable Long heirId,
                                           @Valid @RequestBody HeirUpdateRequest request,
                                           HttpServletRequest httpRequest) {
        Long userId = RequestUtil.getCurrentUserId(httpRequest);
        return Result.success("继承人已更新", heirService.updateHeir(userId, heirId, request));
    }

    /**
     * 删除继承人
     * DELETE /api/heir/{heirId}
     */
    @DeleteMapping("/{heirId}")
    public Result<String> deleteHeir(@PathVariable Long heirId, HttpServletRequest request) {
        Long userId = RequestUtil.getCurrentUserId(request);
        heirService.deleteHeir(userId, heirId);
        return Result.success("继承人已删除");
    }

    /**
     * 重发继承人确认邀请
     * POST /api/heir/{heirId}/resend-invite
     */
    @PostMapping("/{heirId}/resend-invite")
    public Result<String> resendInvite(@PathVariable Long heirId, HttpServletRequest request) {
        Long userId = RequestUtil.getCurrentUserId(request);
        heirService.resendInvite(userId, heirId);
        return Result.success("确认邀请已重发");
    }

    /**
     * 确认继承人邀请（无需登录，继承人访问确认链接）
     * POST /api/heir/confirm
     */
    @PostMapping("/confirm")
    public Result<String> confirmInvite(@RequestParam String token) {
        heirService.confirmHeirInvite(token);
        return Result.success("继承确认成功");
    }

    /**
     * 为继承人分配内容（差异化分配）
     * PUT /api/heir/{heirId}/assign
     */
    @PutMapping("/{heirId}/assign")
    public Result<String> assignContent(@PathVariable Long heirId,
                                        @RequestBody HeirAssignRequest request,
                                        HttpServletRequest httpRequest) {
        Long userId = RequestUtil.getCurrentUserId(httpRequest);
        heirService.assignContent(userId, heirId, request);
        return Result.success("内容已分配");
    }

    /**
     * 设置继承人解锁门槛
     * PUT /api/heir/unlock-threshold
     */
    @PutMapping("/unlock-threshold")
    public Result<String> setUnlockThreshold(@Valid @RequestBody UnlockThresholdRequest request,
                                              HttpServletRequest httpRequest) {
        Long userId = RequestUtil.getCurrentUserId(httpRequest);
        heirService.setUnlockThreshold(userId, request);
        return Result.success("解锁门槛已更新");
    }

    /**
     * 查询解锁门槛
     * GET /api/heir/unlock-threshold
     */
    @GetMapping("/unlock-threshold")
    public Result<Integer> getUnlockThreshold(HttpServletRequest request) {
        Long userId = RequestUtil.getCurrentUserId(request);
        return Result.success(heirService.getUnlockThreshold(userId));
    }
}
