package com.legacyvault.module.heartbeat.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 心跳配置实体类
 * 对应表：heartbeat_config
 *
 * @author LegacyVault
 */
@Data
@TableName("heartbeat_config")
public class HeartbeatConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 打卡周期（天） */
    private Integer checkInPeriodDays;

    /** 下次打卡截止日期 */
    private LocalDateTime nextDeadline;

    /** 上次打卡时间 */
    private LocalDateTime lastCheckInAt;

    /** T-14天提醒是否已发送 */
    @TableField("remind_14d_sent")
    private Integer remind14dSent;

    /** T-7天提醒是否已发送 */
    @TableField("remind_7d_sent")
    private Integer remind7dSent;

    /** T-3天提醒是否已发送 */
    @TableField("remind_3d_sent")
    private Integer remind3dSent;

    /** T+0超时提醒是否已发送 */
    @TableField("remind_0d_sent")
    private Integer remind0dSent;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
