package com.legacyvault.module.user.service;

import com.legacyvault.module.user.dto.HeirRequest;
import com.legacyvault.module.user.dto.HeirResponse;

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
     * 删除继承人
     */
    void deleteHeir(Long userId, Long heirId);

    /**
     * 确认继承人邀请（继承人访问确认链接时调用）
     */
    void confirmHeirInvite(String token);

    /**
     * 为继承人分配内容
     */
    void assignContent(Long userId, Long heirId, List<Long> contentIds);
}
