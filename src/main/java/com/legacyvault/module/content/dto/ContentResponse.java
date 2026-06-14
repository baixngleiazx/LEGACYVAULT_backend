package com.legacyvault.module.content.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 加密内容响应VO（不返回密文，仅返回元数据）
 */
@Data
public class ContentResponse {
    private Long id;
    private String contentType;
    private String contentTypeText;
    private String title;
    private String contentHash;
    private String fileName;
    private Long fileSize;
    private Integer status;
    private Integer shardCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
