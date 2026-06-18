package com.legacyvault.module.delivery.controller;

import com.legacyvault.common.Result;
import com.legacyvault.module.delivery.dto.DeliveryContentResponse;
import com.legacyvault.module.delivery.dto.DeliveryLinkResponse;
import com.legacyvault.module.delivery.dto.DeliveryOtpRequest;
import com.legacyvault.module.delivery.dto.DeliveryVerifyRequest;
import com.legacyvault.module.delivery.service.DeliveryService;
import com.legacyvault.util.RequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

/**
 * 遗产交付控制器
 *
 * @author LegacyVault
 */
@RestController
@RequestMapping("/delivery")
public class DeliveryController {

    @Autowired
    private DeliveryService deliveryService;

    /**
     * 获取交付链接列表（用户视角）
     * GET /api/delivery/links
     */
    @GetMapping("/links")
    public Result<List<DeliveryLinkResponse>> listLinks(HttpServletRequest request) {
        Long userId = RequestUtil.getCurrentUserId(request);
        return Result.success(deliveryService.listDeliveryLinks(userId));
    }

    /**
     * 继承人身份核验并获取解密内容（继承人访问，无需登录）
     * POST /api/delivery/verify
     */
    @PostMapping("/verify")
    public Result<List<DeliveryContentResponse>> verifyAndDecrypt(
            @Valid @RequestBody DeliveryVerifyRequest request,
            HttpServletRequest httpRequest) {
        String ip = RequestUtil.getIpAddress(httpRequest);
        String ua = RequestUtil.getUserAgent(httpRequest);
        return Result.success(deliveryService.verifyAndDecrypt(request, ip, ua));
    }

    /**
     * 发送/重发交付核验 OTP（继承人访问，无需登录）
     * POST /api/delivery/send-otp
     */
    @PostMapping("/send-otp")
    public Result<String> sendOtp(@Valid @RequestBody DeliveryOtpRequest request) {
        deliveryService.sendDeliveryOtp(request);
        return Result.success("验证码已发送");
    }
}
