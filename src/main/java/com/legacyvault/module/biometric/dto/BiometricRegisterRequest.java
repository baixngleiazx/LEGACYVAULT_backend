package com.legacyvault.module.biometric.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 生物特征录入请求 DTO
 *
 * @author LegacyVault
 */
@Data
public class BiometricRegisterRequest {

    /** 生物类型：FACE / FINGER */
    @NotBlank(message = "生物类型不能为空")
    private String biometricType;

    /** 加密特征标识（前端哈希后的值，禁止传原始图像） */
    @NotBlank(message = "特征标识不能为空")
    private String featureHash;

    /** 设备信息（UA 或设备型号） */
    private String deviceInfo;
}
