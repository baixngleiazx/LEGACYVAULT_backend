package com.legacyvault.module.heartbeat.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 提醒记录实体类
 * 对应表：remind_record
 *
 * @author LegacyVault
 */
@Data
@TableName("remind_record")
public class RemindRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 心跳配置ID */
    private Long heartbeatConfigId;

    /** 提醒类型：remind_14d/remind_7d/remind_3d/remind_0d */
    private String remindType;

    /** 渠道：email/sms/push/phone */
    private String channel;

    /** 发送目标 */
    private String target;

    /** 发送状态：0-待发送 1-已发送 2-发送失败 */
    private Integer sendStatus;

    /** 实际发送时间 */
    private LocalDateTime sendAt;

    /** Mock发送数据记录（JSON） */
    private String mockData;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
