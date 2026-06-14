package com.legacyvault.module.content.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 密钥分片记录实体类
 * 对应表：key_shard
 *
 * @author LegacyVault
 */
@Data
@TableName("key_shard")
public class KeyShard implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 关联加密内容ID */
    private Long contentId;

    /** 分片序号：1-K1本地 2-K2 HSM 3-K3第三方 */
    private Integer shardIndex;

    /** 分片数据（加密存储） */
    private String shardData;

    /** 存储位置：local/hsm/third_party */
    private String storageLocation;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
