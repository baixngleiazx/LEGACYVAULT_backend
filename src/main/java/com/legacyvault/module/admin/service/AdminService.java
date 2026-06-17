package com.legacyvault.module.admin.service;

import com.legacyvault.common.PageResult;
import com.legacyvault.module.admin.dto.AdminDashboardVo;
import com.legacyvault.module.admin.dto.AdminUserListVo;
import com.legacyvault.module.admin.dto.AuditLogVo;
import com.legacyvault.module.user.dto.HeirResponse;

import java.util.List;

/**
 * 管理员服务接口
 *
 * @author LegacyVault
 */
public interface AdminService {

    /**
     * 管理员仪表盘统计
     */
    AdminDashboardVo dashboard();

    /**
     * 用户列表（分页 + 各步骤状态 + 继承人数量）
     */
    PageResult<AdminUserListVo> listUsers(int page, int size, String keyword, Integer status);

    /**
     * 用户详情（敏感字段脱敏：不含 TOTP 密钥 / 恢复码 / 证件明文 / 生物原始图像）
     */
    AdminUserListVo getUserDetail(Long userId);

    /**
     * 用户继承人只读查询
     */
    List<HeirResponse> getUserHeirs(Long userId);

    /**
     * 审计日志分页查询
     */
    PageResult<AuditLogVo> listAuditLogs(int page, int size, String module, Long userId);
}
