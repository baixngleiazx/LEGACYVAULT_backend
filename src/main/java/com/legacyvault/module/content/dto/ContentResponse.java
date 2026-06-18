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
    /** 仅详情接口返回，列表接口不返回。 */
    private String encryptedData;
    /** 仅详情接口返回，委托人本人查看时与本地K1重组密钥；不返回K3。 */
    private String k2Shard;
    private String fileName;
    private Long fileSize;
    private Integer status;
    private Integer shardCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
