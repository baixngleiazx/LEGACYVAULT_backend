package com.legacyvault.module.content.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 加密内容实体类
 * 对应表：encrypted_content
 *
 * @author LegacyVault
 */
@Data
@TableName("encrypted_content")
public class EncryptedContent implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户ID */
    private Long userId;

    /** 内容类型：private_key/account_password/last_words/file */
    private String contentType;

    /** 内容标题 */
    private String title;

    /** 加密后的数据（Base64编码） */
    private String encryptedData;

    /** 原始内容SHA-256哈希 */
    private String contentHash;

    /** 原始文件名（仅file类型） */
    private String fileName;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 密文存储路径 */
    private String storagePath;

    /** K2分片（HSM托管） */
    private String k2Shard;

    /** K3分片（第三方节点托管） */
    private String k3Shard;

    /** 状态：0-已删除 1-正常 2-已交付 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
