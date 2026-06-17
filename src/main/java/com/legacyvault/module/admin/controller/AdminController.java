package com.legacyvault.module.admin.controller;

import com.legacyvault.common.PageResult;
import com.legacyvault.common.Result;
import com.legacyvault.module.admin.dto.*;
import com.legacyvault.module.admin.service.AdminService;
import com.legacyvault.module.admin.service.SysConfigService;
import com.legacyvault.module.kyc.dto.KycRecordVo;
import com.legacyvault.module.kyc.service.KycService;
import com.legacyvault.module.user.dto.HeirResponse;
import com.legacyvault.util.RequestUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

/**
 * 管理员控制器
 * 所有接口均受 AdminAuthInterceptor 保护
 *
 * @author LegacyVault
 */
@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private KycService kycService;

    @Autowired
    private SysConfigService sysConfigService;

    // ===== 仪表盘 =====

    /**
     * 仪表盘统计
     * GET /api/admin/dashboard
     */
    @GetMapping("/dashboard")
    public Result<AdminDashboardVo> dashboard() {
        return Result.success(adminService.dashboard());
    }

    // ===== 用户管理 =====

    /**
     * 用户列表（分页）
     * GET /api/admin/users
     */
    @GetMapping("/users")
    public Result<PageResult<AdminUserListVo>> listUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return Result.success(adminService.listUsers(page, size, keyword, status));
    }

    /**
     * 用户详情（敏感字段脱敏）
     * GET /api/admin/users/{userId}
     */
    @GetMapping("/users/{userId}")
    public Result<AdminUserListVo> getUserDetail(@PathVariable Long userId) {
        return Result.success(adminService.getUserDetail(userId));
    }

    /**
     * 用户继承人只读查询
     * GET /api/admin/users/{userId}/heirs
     */
    @GetMapping("/users/{userId}/heirs")
    public Result<List<HeirResponse>> getUserHeirs(@PathVariable Long userId) {
        return Result.success(adminService.getUserHeirs(userId));
    }

    // ===== KYC 审核 =====

    /**
     * KYC 待审列表
     * GET /api/admin/kyc/pending
     */
    @GetMapping("/kyc/pending")
    public Result<PageResult<KycRecordVo>> listPendingKyc(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(kycService.listPending(page, size));
    }

    /**
     * KYC 全量列表
     * GET /api/admin/kyc/list
     */
    @GetMapping("/kyc/list")
    public Result<PageResult<KycRecordVo>> listAllKyc(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer status) {
        return Result.success(kycService.listAll(page, size, status));
    }

    /**
     * KYC 通过
     * POST /api/admin/kyc/{recordId}/approve
     */
    @PostMapping("/kyc/{recordId}/approve")
    public Result<String> approveKyc(@PathVariable Long recordId, HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("currentAdminId");
        kycService.approve(recordId, adminId);
        return Result.success("KYC 审核已通过");
    }

    /**
     * KYC 驳回
     * POST /api/admin/kyc/{recordId}/reject
     */
    @PostMapping("/kyc/{recordId}/reject")
    public Result<String> rejectKyc(@PathVariable Long recordId,
                                     @Valid @RequestBody AdminRejectRequest body,
                                     HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("currentAdminId");
        kycService.reject(recordId, adminId, body.getRejectReason());
        return Result.success("KYC 审核已驳回");
    }

    // ===== 系统配置 =====

    /**
     * 查询所有系统配置
     * GET /api/admin/config
     */
    @GetMapping("/config")
    public Result<List<SysConfigVo>> getConfig() {
        return Result.success(sysConfigService.listAll());
    }

    /**
     * 批量更新系统配置
     * PUT /api/admin/config
     */
    @PutMapping("/config")
    public Result<String> updateConfig(@Valid @RequestBody SysConfigUpdateRequest body,
                                       HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("currentAdminId");
        sysConfigService.batchUpdate(adminId, body.getConfigs());
        return Result.success("系统配置已更新");
    }

    // ===== 审计日志 =====

    /**
     * 审计日志分页查询
     * GET /api/admin/audit-logs
     */
    @GetMapping("/audit-logs")
    public Result<PageResult<AuditLogVo>> listAuditLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) Long userId) {
        return Result.success(adminService.listAuditLogs(page, size, module, userId));
    }
}
