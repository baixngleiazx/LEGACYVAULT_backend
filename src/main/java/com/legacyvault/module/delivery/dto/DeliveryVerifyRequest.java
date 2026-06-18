package com.legacyvault.module.delivery.dto;

import lombok.Data;
import javax.validation.constraints.*;

/**
 * 继承人交付身份核验请求DTO
 */
@Data
public class DeliveryVerifyRequest {
    @NotBlank(message = "链接Token不能为空")
    private String linkToken;

    @NotBlank(message = "邮箱OTP不能为空")
    private String emailOtp;

    @NotBlank(message = "手机OTP不能为空")
    private String phoneOtp;

    /** 人脸图片Base64。Mock模式仍需提交采集占位数据，保证三重核验流程一致。 */
    @NotBlank(message = "人脸核验数据不能为空")
    private String faceImageBase64;

    /** 设备指纹 */
    private String deviceFingerprint;
}
