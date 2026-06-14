package com.legacyvault.module.delivery.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 交付内容响应VO（解密后的内容展示）
 */
@Data
public class DeliveryContentResponse {
    private Long contentId;
    private String contentType;
    private String contentTypeText;
    private String title;
    private String decryptedData;
    private String fileName;
    private Long fileSize;
    private String blockchainTxHash;
    private LocalDateTime accessedAt;
}
