package com.legacyvault.module.delivery.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 交付链接实体类
 * 对应表：delivery_link
 *
 * @author LegacyVault
 */
@Data
@TableName("delivery_link")
public class DeliveryLink implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 原用户ID */
    private Long userId;

    /** 继承人ID */
    private Long heirId;

    /** 触发流程ID */
    private Long triggerProcessId;

    /** 一次性JWT票据 */
    private String jwtToken;

    /** 链接唯一标识（URL安全） */
    private String linkToken;

    /** 状态：0-已失效 1-有效 2-已使用 3-已锁定 */
    private Integer status;

    /** 过期时间 */
    private LocalDateTime expiresAt;

    /** 使用时间 */
    private LocalDateTime usedAt;

    /** 核验失败次数 */
    private Integer failCount;

    /** 最大失败次数 */
    private Integer maxFailCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
