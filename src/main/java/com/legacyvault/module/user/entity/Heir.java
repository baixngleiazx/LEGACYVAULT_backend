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

    /** 确认状态：0-待确认 1-已确认 2-已拒绝 */
    private Integer confirmationStatus;

    /** 确认邀请Token */
    private String confirmationToken;

    /** 确认时间 */
    private LocalDateTime confirmedAt;

    /** 分配的内容数量 */
    private Integer assignedContentCount;

    /** 排序号 */
    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
