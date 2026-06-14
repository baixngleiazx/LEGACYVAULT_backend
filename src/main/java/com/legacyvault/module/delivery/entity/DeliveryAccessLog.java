package com.legacyvault.module.delivery.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 交付访问记录实体类
 * 对应表：delivery_access_log
 *
 * @author LegacyVault
 */
@Data
@TableName("delivery_access_log")
public class DeliveryAccessLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 交付链接ID */
    private Long deliveryLinkId;

    /** 继承人ID */
    private Long heirId;

    /** 访问类型：identity_check/decrypt/view */
    private String accessType;

    /** 访问IP */
    private String ipAddress;

    /** 设备指纹 */
    private String deviceFingerprint;

    /** 浏览器UA */
    private String userAgent;

    /** 结果：SUCCESS/FAILED */
    private String result;

    /** 上链存证交易哈希 */
    private String blockchainTxHash;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
