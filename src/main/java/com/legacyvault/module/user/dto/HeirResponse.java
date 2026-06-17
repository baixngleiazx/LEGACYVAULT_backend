package com.legacyvault.module.user.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 继承人响应 VO
 *
 * @author LegacyVault
 */
@Data
public class HeirResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;

    /** 细化状态：0草稿 1已发邀请 2已确认 3已拒绝 */
    private Integer status;
    private Integer confirmationStatus;
    private String confirmationStatusText;

    /** 分配的内容数量 */
    private Integer assignedContentCount;

    /** 分配的具体内容 ID 列表 */
    private List<Long> assignedContentIds;

    /** 首次邀请发送时间 */
    private LocalDateTime invitedAt;

    /** 最后邀请发送时间 */
    private LocalDateTime lastInviteSentAt;

    private LocalDateTime confirmedAt;
    private LocalDateTime createdAt;
}
