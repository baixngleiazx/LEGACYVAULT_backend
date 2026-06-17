package com.legacyvault.module.kyc.controller;

import com.legacyvault.common.Result;
import com.legacyvault.module.kyc.dto.KycStatusResponse;
import com.legacyvault.module.kyc.dto.KycSubmitRequest;
import com.legacyvault.module.kyc.service.KycService;
import com.legacyvault.util.RequestUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * KYC 控制器（用户侧）
 *
 * @author LegacyVault
 */
@Slf4j
@RestController
@RequestMapping("/kyc")
public class KycController {

    @Autowired
    private KycService kycService;

    /**
     * 提交 KYC 材料
     * POST /api/kyc/submit
     */
    @PostMapping("/submit")
    public Result<String> submit(@Valid @RequestBody KycSubmitRequest request,
                                 HttpServletRequest httpRequest) {
        Long userId = RequestUtil.getCurrentUserId(httpRequest);
        kycService.submit(userId, request);
        return Result.success("KYC 材料已提交");
    }

    /**
     * 查询当前用户 KYC 状态
     * GET /api/kyc/status
     */
    @GetMapping("/status")
    public Result<KycStatusResponse> getStatus(HttpServletRequest request) {
        Long userId = RequestUtil.getCurrentUserId(request);
        return Result.success(kycService.getStatus(userId));
    }
}
