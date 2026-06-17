package com.legacyvault.module.registration.service;

import com.legacyvault.module.registration.dto.RegistrationStatusResponse;

/**
 * 注册流程服务接口
 *
 * @author LegacyVault
 */
public interface RegistrationService {

    /**
     * 查询当前用户的 5 步注册流程状态
     */
    RegistrationStatusResponse getStatus(Long userId);

    /**
     * 标记某一步骤为"已完成"
     */
    void completeStep(Long userId, int step);

    /**
     * 标记某一步骤为"已跳过"
     */
    void skipStep(Long userId, int step);

    /**
     * 标记整个注册流程完成（触发后置动作）
     */
    void completeAll(Long userId);
}
