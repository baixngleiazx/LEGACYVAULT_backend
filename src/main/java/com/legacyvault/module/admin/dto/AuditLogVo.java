package com.legacyvault.module.admin.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审计日志 VO（管理员查询用）
 *
 * @author LegacyVault
 */
@Data
public class AuditLogVo {

    private Long id;
    private Long userId;
    private String module;
    private String action;
    private String targetType;
    private Long targetId;
    private String detail;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime createdAt;
}
