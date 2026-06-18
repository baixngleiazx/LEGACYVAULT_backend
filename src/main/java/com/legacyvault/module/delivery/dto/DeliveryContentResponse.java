package com.legacyvault.module.delivery.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 交付内容响应VO。后端只返回密文和授权分片，浏览器负责本地解密。
 */
@Data
public class DeliveryContentResponse {
    private Long contentId;
    private String contentType;
    private String contentTypeText;
    private String title;
    private String encryptedData;
    private String contentHash;
    private String k2Shard;
    private String k3Shard;
    /** 兼容旧前端字段；新前端不依赖后端返回明文。 */
    private String decryptedData;
    private String fileName;
    private Long fileSize;
    private String blockchainTxHash;
    private LocalDateTime accessedAt;
}
