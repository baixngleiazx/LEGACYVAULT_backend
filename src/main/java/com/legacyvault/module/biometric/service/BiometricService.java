package com.legacyvault.module.biometric.service;

import com.legacyvault.module.biometric.dto.BiometricRegisterRequest;
import com.legacyvault.module.biometric.dto.BiometricStatusResponse;

/**
 * 生物特征服务接口
 *
 * @author LegacyVault
 */
public interface BiometricService {

    /**
     * 录入生物特征标识（仅存加密哈希，不存原始图像）
     */
    void register(Long userId, BiometricRegisterRequest request);

    /**
     * 查询生物特征绑定状态
     */
    BiometricStatusResponse getStatus(Long userId);
}
