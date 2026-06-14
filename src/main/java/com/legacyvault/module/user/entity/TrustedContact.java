package com.legacyvault.module.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 可信联系人实体类
 * 对应表：trusted_contact
 *
 * @author LegacyVault
 */
@Data
@TableName("trusted_contact")
public class TrustedContact implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 联系人姓名 */
    private String name;

    /** 联系人邮箱 */
    private String email;

    /** 联系人手机号 */
    private String phone;

    /** 与用户关系：family/friend/lawyer/other */
    private String relationship;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
