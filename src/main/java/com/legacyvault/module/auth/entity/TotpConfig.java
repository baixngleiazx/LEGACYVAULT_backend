package com.legacyvault.module.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * TOTP配置实体类
 * 对应表：totp_config
 *
 * @author LegacyVault
 */
@Data
@TableName("totp_config")
public class TotpConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** TOTP密钥（加密存储） */
    private String secretKey;

    /** 发行方名称 */
    private String issuer;

    /** 设备类型：app/hardware */
    private String deviceType;

    /** 绑定时间 */
    private LocalDateTime boundAt;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
