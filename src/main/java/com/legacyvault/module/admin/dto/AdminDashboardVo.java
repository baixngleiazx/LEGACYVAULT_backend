package com.legacyvault.module.admin.dto;

import lombok.Data;

/**
 * 管理员仪表盘统计数据 VO
 *
 * @author LegacyVault
 */
@Data
public class AdminDashboardVo {

    /** 总用户数 */
    private Long totalUsers;

    /** 注册流程进行中（5 步未全部完成） */
    private Long registrationInProgress;

    /** 注册流程已完成 */
    private Long registrationCompleted;

    /** KYC 待审核 */
    private Long kycPending;

    /** KYC 已通过 */
    private Long kycPassed;

    /** 今日登录用户数 */
    private Long todayActiveUsers;
}
