package com.legacyvault.module.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 继承人实体类
 * 对应表：heir
 *
 * @author LegacyVault
 */
@Data
@TableName("heir")
public class Heir implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户ID */
    private Long userId;

    /** 继承人姓名 */
    private String name;

    /** 继承人邮箱 */
    private String email;

    /** 继承人手机号 */
    private String phone;

    /** 证件号（加密存储） */
    private String idCardNo;

    /** 确认状态：0-待确认 1-已确认 2-已拒绝（兼容旧字段） */
    private Integer confirmationStatus;

    /** 细化状态：0草稿 1已发邀请 2已确认 3已拒绝 */
    private Integer status;

    /** 确认邀请Token */
    private String confirmationToken;

    /** 首次邀请发送时间 */
    private LocalDateTime invitedAt;

    /** 最后一次邀请发送时间 */
    private LocalDateTime lastInviteSentAt;

    /** 确认时间 */
    private LocalDateTime confirmedAt;

    /** 分配的内容数量 */
    private Integer assignedContentCount;

    /** 加密证件号（仅 Pro 高资产用户开放录入） */
    private String idNoEncrypted;

    /** 排序号 */
    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
