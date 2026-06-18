package com.legacyvault.module.delivery.service;

import com.legacyvault.module.delivery.dto.DeliveryContentResponse;
import com.legacyvault.module.delivery.dto.DeliveryLinkResponse;
import com.legacyvault.module.delivery.dto.DeliveryOtpRequest;
import com.legacyvault.module.delivery.dto.DeliveryVerifyRequest;

import java.util.List;

/**
 * 遗产交付服务接口
 *
 * @author LegacyVault
 */
public interface DeliveryService {

    /**
     * 获取用户的交付链接列表
     */
    List<DeliveryLinkResponse> listDeliveryLinks(Long userId);

    /**
     * 继承人身份核验并获取解密内容
     */
    List<DeliveryContentResponse> verifyAndDecrypt(DeliveryVerifyRequest request, String ipAddress, String userAgent);

    /**
     * 发送交付核验 OTP。
     */
    void sendDeliveryOtp(DeliveryOtpRequest request);
}
