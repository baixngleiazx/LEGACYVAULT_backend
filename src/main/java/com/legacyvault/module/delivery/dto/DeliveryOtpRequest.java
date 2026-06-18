package com.legacyvault.module.delivery.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 交付核验 OTP 发送请求。
 */
@Data
public class DeliveryOtpRequest {

    @NotBlank(message = "链接Token不能为空")
    private String linkToken;

    @NotBlank(message = "渠道不能为空")
    private String channel; // email / phone
}
