package com.legacyvault.module.user.controller;

import com.legacyvault.common.Result;
import com.legacyvault.module.user.dto.HeirRequest;
import com.legacyvault.module.user.dto.HeirResponse;
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
     * 确认继承人邀请（无需登录，继承人访问确认链接）
     * POST /api/heir/confirm
     */
    @PostMapping("/confirm")
    public Result<String> confirmInvite(@RequestParam String token) {
        heirService.confirmHeirInvite(token);
        return Result.success("继承确认成功");
    }

    /**
     * 为继承人分配内容
     * POST /api/heir/{heirId}/assign
     */
    @PostMapping("/{heirId}/assign")
    public Result<String> assignContent(@PathVariable Long heirId, @RequestBody List<Long> contentIds, HttpServletRequest request) {
        Long userId = RequestUtil.getCurrentUserId(request);
        heirService.assignContent(userId, heirId, contentIds);
        return Result.success("内容已分配");
    }
}
