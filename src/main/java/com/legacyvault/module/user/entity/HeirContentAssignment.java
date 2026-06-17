package com.legacyvault.module.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 继承人内容分配明细实体
 * 对应表：heir_content_assignment
 *
 * @author LegacyVault
 */
@Data
@TableName("heir_content_assignment")
public class HeirContentAssignment implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 继承人ID */
    private Long heirId;

    /** 内容ID */
    private Long contentId;

    /** 分配时间 */
    private LocalDateTime assignedAt;
}
