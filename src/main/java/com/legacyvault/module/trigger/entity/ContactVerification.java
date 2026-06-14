package com.legacyvault.module.trigger.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 可信联系人核查记录实体类
 * 对应表：contact_verification
 *
 * @author LegacyVault
 */
@Data
@TableName("contact_verification")
public class ContactVerification implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 触发流程ID */
    private Long triggerProcessId;

    /** 可信联系人ID */
    private Long trustedContactId;

    /** 核查状态：0-待回复 1-确认失联 2-确认活跃 */
    private Integer verificationStatus;

    /** 回复时间 */
    private LocalDateTime replyAt;

    /** 回复渠道 */
    private String replyChannel;

    /** Mock回复数据 */
    private String mockData;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
