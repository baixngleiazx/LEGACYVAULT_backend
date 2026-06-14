package com.legacyvault.module.trigger.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 触发阶段记录实体类
 * 对应表：trigger_stage_record
 *
 * @author LegacyVault
 */
@Data
@TableName("trigger_stage_record")
public class TriggerStageRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 触发流程ID */
    private Long triggerProcessId;

    /** 阶段：T0/T72/T96/T120 */
    private String stage;

    /** 动作描述 */
    private String action;

    /** 结果：SUCCESS/FAILED/PENDING */
    private String result;

    /** 详细记录（JSON） */
    private String detail;

    /** Mock数据（JSON） */
    private String mockData;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
