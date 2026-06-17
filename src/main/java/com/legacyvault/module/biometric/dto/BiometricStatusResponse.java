package com.legacyvault.module.biometric.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 生物特征状态响应 DTO
 *
 * @author LegacyVault
 */
@Data
public class BiometricStatusResponse {

    /** 是否已录入 */
    private Boolean bound;

    /** 生物类型（FACE / FINGER，未录入时为 null） */
    private String biometricType;

    /** 设备信息 */
    private String deviceInfo;

    /** 绑定时间 */
    private LocalDateTime boundAt;
}
