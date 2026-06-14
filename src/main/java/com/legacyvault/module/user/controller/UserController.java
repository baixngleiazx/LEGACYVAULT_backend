package com.legacyvault.module.user.controller;

import com.legacyvault.common.Result;
import com.legacyvault.module.user.dto.UserInfoResponse;
import com.legacyvault.module.user.service.UserService;
import com.legacyvault.util.RequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 用户管理控制器
 *
 * @author LegacyVault
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取当前用户信息
     * GET /api/user/info
     */
    @GetMapping("/info")
    public Result<UserInfoResponse> getUserInfo(HttpServletRequest request) {
        Long userId = RequestUtil.getCurrentUserId(request);
        return Result.success(userService.getUserInfo(userId));
    }

    /**
     * 更新昵称
     * PUT /api/user/nickname
     */
    @PutMapping("/nickname")
    public Result<String> updateNickname(@RequestBody Map<String, String> body, HttpServletRequest request) {
        Long userId = RequestUtil.getCurrentUserId(request);
        userService.updateNickname(userId, body.get("nickname"));
        return Result.success("昵称已更新");
    }

    /**
     * 提交KYC核验
     * POST /api/user/kyc
     */
    @PostMapping("/kyc")
    public Result<String> submitKyc(@RequestBody Map<String, String> body, HttpServletRequest request) {
        Long userId = RequestUtil.getCurrentUserId(request);
        userService.submitKyc(userId, body.get("name"), body.get("idCardNo"));
        return Result.success("KYC核验已提交");
    }
}
