package com.legacyvault.module.user.service;

import com.legacyvault.module.user.dto.UserInfoResponse;

/**
 * 用户服务接口
 *
 * @author LegacyVault
 */
public interface UserService {

    /**
     * 获取当前用户信息
     */
    UserInfoResponse getUserInfo(Long userId);

    /**
     * 更新用户昵称
     */
    void updateNickname(Long userId, String nickname);

    /**
     * 提交KYC核验（Mock）
     */
    void submitKyc(Long userId, String name, String idCardNo);
}
