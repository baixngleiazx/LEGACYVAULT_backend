package com.legacyvault.module.content.controller;

import com.legacyvault.common.Result;
import com.legacyvault.module.content.dto.ContentRequest;
import com.legacyvault.module.content.dto.ContentResponse;
import com.legacyvault.module.content.service.ContentService;
import com.legacyvault.util.RequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

/**
 * 加密内容管理控制器
 *
 * @author LegacyVault
 */
@RestController
@RequestMapping("/content")
public class ContentController {

    @Autowired
    private ContentService contentService;

    /**
     * 获取加密内容列表
     * GET /api/content/list?type=xxx
     */
    @GetMapping("/list")
    public Result<List<ContentResponse>> listContents(
            @RequestParam(required = false) String type,
            HttpServletRequest request) {
        Long userId = RequestUtil.getCurrentUserId(request);
        return Result.success(contentService.listContents(userId, type));
    }

    /**
     * 创建加密内容
     * POST /api/content/create
     */
    @PostMapping("/create")
    public Result<ContentResponse> createContent(@Valid @RequestBody ContentRequest request, HttpServletRequest httpRequest) {
        Long userId = RequestUtil.getCurrentUserId(httpRequest);
        return Result.success("加密内容已保存", contentService.createContent(userId, request));
    }

    /**
     * 删除加密内容
     * DELETE /api/content/{contentId}
     */
    @DeleteMapping("/{contentId}")
    public Result<String> deleteContent(@PathVariable Long contentId, HttpServletRequest request) {
        Long userId = RequestUtil.getCurrentUserId(request);
        contentService.deleteContent(userId, contentId);
        return Result.success("内容已删除");
    }

    /**
     * 获取加密内容详情（仅元数据，不含密文）
     * GET /api/content/{contentId}
     */
    @GetMapping("/{contentId}")
    public Result<ContentResponse> getContentDetail(@PathVariable Long contentId, HttpServletRequest request) {
        Long userId = RequestUtil.getCurrentUserId(request);
        return Result.success(contentService.getContentDetail(userId, contentId));
    }
}
