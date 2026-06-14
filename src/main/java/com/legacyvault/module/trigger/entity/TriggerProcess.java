package com.legacyvault.module.trigger.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 触发流程实体类
 * 对应表：trigger_process
 *
 * @author LegacyVault
 */
@Data
@TableName("trigger_process")
public class TriggerProcess implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 心跳配置ID */
    private Long heartbeatConfigId;

    /** 流程状态 */
    private String status;

    /** T+0宽限期开始时间 */
    private LocalDateTime gracePeriodStart;

    /** T+72h联系人核查开始时间 */
    private LocalDateTime contactCheckStart;

    /** T+96h公证人通知时间 */
    private LocalDateTime notaryNotifyAt;

    /** T+120h最终确认时间 */
    private LocalDateTime finalConfirmAt;

    /** 流程完成时间 */
    private LocalDateTime completedAt;

    /** 流程中止时间 */
    private LocalDateTime abortedAt;

    /** 中止原因 */
    private String abortReason;

    /** 中止方：user/contact/recovery_code */
    private String abortBy;

    /** 区块链存证交易哈希 */
    private String blockchainTxHash;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
