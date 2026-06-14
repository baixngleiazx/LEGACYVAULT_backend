package com.legacyvault.module.user.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 继承人响应VO
 */
@Data
public class HeirResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private Integer confirmationStatus;
    private String confirmationStatusText;
    private Integer assignedContentCount;
    private LocalDateTime confirmedAt;
    private LocalDateTime createdAt;
}
