package com.legacyvault.module.user.controller;

import com.legacyvault.common.Result;
import com.legacyvault.module.user.dto.TrustedContactRequest;
import com.legacyvault.module.user.entity.TrustedContact;
import com.legacyvault.module.user.service.TrustedContactService;
import com.legacyvault.util.RequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

/**
 * 可信联系人管理控制器
 *
 * @author LegacyVault
 */
@RestController
@RequestMapping("/trusted-contact")
public class TrustedContactController {

    @Autowired
    private TrustedContactService trustedContactService;

    /**
     * 获取可信联系人列表
     * GET /api/trusted-contact/list
     */
    @GetMapping("/list")
    public Result<List<TrustedContact>> listContacts(HttpServletRequest request) {
        Long userId = RequestUtil.getCurrentUserId(request);
        return Result.success(trustedContactService.listContacts(userId));
    }

    /**
     * 添加可信联系人
     * POST /api/trusted-contact/add
     */
    @PostMapping("/add")
    public Result<TrustedContact> addContact(@Valid @RequestBody TrustedContactRequest request, HttpServletRequest httpRequest) {
        Long userId = RequestUtil.getCurrentUserId(httpRequest);
        return Result.success("可信联系人已添加", trustedContactService.addContact(userId, request));
    }

    /**
     * 删除可信联系人
     * DELETE /api/trusted-contact/{contactId}
     */
    @DeleteMapping("/{contactId}")
    public Result<String> deleteContact(@PathVariable Long contactId, HttpServletRequest request) {
        Long userId = RequestUtil.getCurrentUserId(request);
        trustedContactService.deleteContact(userId, contactId);
        return Result.success("可信联系人已删除");
    }
}
