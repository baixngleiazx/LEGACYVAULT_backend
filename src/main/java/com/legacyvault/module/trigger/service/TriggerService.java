package com.legacyvault.module.trigger.service;

import com.legacyvault.module.trigger.dto.ContactReplyRequest;
import com.legacyvault.module.trigger.dto.TriggerProcessResponse;

import java.util.List;

/**
 * 触发验证引擎服务接口
 * 管理心跳超时后的多阶段触发流程
 *
 * @author LegacyVault
 */
public interface TriggerService {

    /**
     * 获取用户的触发流程列表
     */
    List<TriggerProcessResponse> listProcesses(Long userId);

    /**
     * 获取最新触发流程详情
     */
    TriggerProcessResponse getLatestProcess(Long userId);

    /**
     * 中止触发流程（用户登录中止）
     */
    void abortProcess(Long userId, String totpCode);

    /**
     * 中止触发流程（恢复码中止）
     */
    void abortProcessByRecoveryCode(Long userId, String recoveryCode);

    /**
     * 可信联系人回复核查
     */
    void replyContactVerification(ContactReplyRequest request);

    /**
     * 定时任务：检查超时用户并启动触发流程
     */
    void checkExpiredHeartbeats();

    /**
     * 定时任务：推进触发流程阶段
     */
    void advanceTriggerProcesses();
}
