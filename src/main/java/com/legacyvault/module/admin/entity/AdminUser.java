package com.legacyvault.module.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 管理员账号实体
 * 对应表：admin_user（与 user 表完全隔离）
 *
 * @author LegacyVault
 */
@Data
@TableName("admin_user")
public class AdminUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 登录账号 */
    private String username;

    /** 密码哈希 */
    private String passwordHash;

    /** 真实姓名 */
    private String realName;

    /** 角色：ADMIN / SUPER_ADMIN */
    private String role;

    /** 状态：0-禁用 1-正常 */
    private Integer status;

    /** 最后登录时间 */
    private LocalDateTime lastLoginAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
