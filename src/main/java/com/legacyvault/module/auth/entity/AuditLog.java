package com.legacyvault.module.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作审计日志实体类
 * 对应表：audit_log
 *
 * @author LegacyVault
 */
@Data
@TableName("audit_log")
public class AuditLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 操作用户ID */
    private Long userId;

    /** 操作模块 */
    private String module;

    /** 操作动作 */
    private String action;

    /** 操作对象类型 */
    private String targetType;

    /** 操作对象ID */
    private Long targetId;

    /** 操作详情（JSON） */
    private String detail;

    /** IP地址 */
    private String ipAddress;

    /** 设备信息 */
    private String userAgent;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
