package com.legacyvault.module.delivery.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 交付链接响应VO（管理员/用户视角）
 */
@Data
public class DeliveryLinkResponse {
    private Long id;
    private Long heirId;
    private String heirName;
    private String linkToken;
    private Integer status;
    private String statusText;
    private LocalDateTime expiresAt;
    private LocalDateTime usedAt;
    private Integer failCount;
    private LocalDateTime createdAt;
}
