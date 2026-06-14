package com.legacyvault.module.content.dto;

import lombok.Data;
import javax.validation.constraints.*;

/**
 * 创建/更新加密内容请求DTO
 */
@Data
public class ContentRequest {
    @NotBlank(message = "内容类型不能为空")
    private String contentType;  // private_key/account_password/last_words/file

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "加密数据不能为空")
    private String encryptedData;

    @NotBlank(message = "内容哈希不能为空")
    private String contentHash;

    /** 原始文件名（仅file类型） */
    private String fileName;

    /** 文件大小 */
    private Long fileSize;

    /** K1分片（用户本地保留） */
    private String k1Shard;

    /** K2分片（托管HSM） */
    private String k2Shard;

    /** K3分片（托管第三方） */
    private String k3Shard;
}
