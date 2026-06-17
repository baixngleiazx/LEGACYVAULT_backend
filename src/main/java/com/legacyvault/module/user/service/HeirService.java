package com.legacyvault.module.user.service;

import com.legacyvault.module.user.dto.*;

import java.util.List;

/**
 * 继承人服务接口
 *
 * @author LegacyVault
 */
public interface HeirService {

    /**
     * 获取用户的所有继承人
     */
    List<HeirResponse> listHeirs(Long userId);

    /**
     * 添加继承人
     */
    HeirResponse addHeir(Long userId, HeirRequest request);

    /**
     * 编辑继承人（仅草稿/待确认状态允许）
     */
    HeirResponse updateHeir(Long userId, Long heirId, HeirUpdateRequest request);

    /**
     * 删除继承人（仅草稿/待确认/已拒绝状态允许）
     */
    void deleteHeir(Long userId, Long heirId);

    /**
     * 重发继承人确认邀请（24 小时冷却）
     */
    void resendInvite(Long userId, Long heirId);

    /**
     * 确认继承人邀请（继承人访问确认链接时调用）
     */
    void confirmHeirInvite(String token);

    /**
     * 为继承人分配内容（差异化分配）
     */
    void assignContent(Long userId, Long heirId, HeirAssignRequest request);

    /**
     * 设置用户的继承人解锁门槛
     */
    void setUnlockThreshold(Long userId, UnlockThresholdRequest request);

    /**
     * 获取用户的解锁门槛
     */
    Integer getUnlockThreshold(Long userId);
}
