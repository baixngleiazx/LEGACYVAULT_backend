package com.legacyvault.module.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 对应表：user
 *
 * @author LegacyVault
 */
@Data
@TableName("user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String phone;

    /** 密码哈希（BCrypt） */
    private String passwordHash;

    /** 昵称 */
    private String nickname;

    /** 状态：0-禁用 1-正常 2-锁定 */
    private Integer status;

    /** 当前套餐ID */
    private Long planId;

    /** 套餐到期时间 */
    private LocalDateTime planExpiresAt;

    /** TOTP是否已绑定：0-否 1-是 */
    private Integer totpBound;

    /** 生物特征是否已录入：0-否 1-是 */
    private Integer biometricBound;

    /** KYC状态：0-未认证 1-已提交 2-已通过 3-已拒绝 */
    private Integer kycStatus;

    /** 安全健康分（0-100） */
    private Integer securityScore;

    /** 旅行模式是否开启：0-否 1-是 */
    private Integer travelModeEnabled;

    /** 旅行模式开始日期 */
    private LocalDateTime travelStartDate;

    /** 旅行模式结束日期 */
    private LocalDateTime travelEndDate;

    /** 最后登录时间 */
    private LocalDateTime lastLoginAt;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 逻辑删除：0-未删除 1-已删除 */
    @TableLogic
    private Integer deleted;
}
