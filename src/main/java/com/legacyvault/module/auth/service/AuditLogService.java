package com.legacyvault.module.auth.service;

import com.legacyvault.module.auth.entity.AuditLog;

/**
 * 审计日志服务接口
 *
 * @author LegacyVault
 */
public interface AuditLogService {

    /**
     * 记录审计日志
     *
     * @param userId     用户ID（可为空）
     * @param module     操作模块
     * @param action     操作动作
     * @param targetType 操作对象类型
     * @param targetId   操作对象ID
     * @param detail     详情（JSON字符串）
     * @param ipAddress  IP地址
     * @param userAgent  设备UA
     */
    void log(Long userId, String module, String action, String targetType, Long targetId, String detail, String ipAddress, String userAgent);

    /**
     * 简化版审计日志（自动从Request获取IP/UA）
     */
    void log(Long userId, String module, String action, String detail);
}
