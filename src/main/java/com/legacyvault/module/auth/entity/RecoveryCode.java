package com.legacyvault.module.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 紧急恢复码实体类
 * 对应表：recovery_code
 *
 * @author LegacyVault
 */
@Data
@TableName("recovery_code")
public class RecoveryCode implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 恢复码哈希（仅存哈希） */
    private String codeHash;

    /** 是否已使用：0-未使用 1-已使用 */
    private Integer isUsed;

    /** 使用时间 */
    private LocalDateTime usedAt;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
