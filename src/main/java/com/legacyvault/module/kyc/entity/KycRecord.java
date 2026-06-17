package com.legacyvault.module.kyc.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * KYC 审核单据实体
 * 对应表：kyc_record
 *
 * @author LegacyVault
 */
@Data
@TableName("kyc_record")
public class KycRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户ID */
    private Long userId;

    /** 真实姓名 */
    private String realName;

    /** 证件类型：ID_CARD / PASSPORT */
    private String idType;

    /** 证件号（AES 加密存储） */
    private String idNoEncrypted;

    /** 证件正面照路径 */
    private String frontImageUrl;

    /** 证件背面照路径 */
    private String backImageUrl;

    /** 活体检测是否通过：0-否 1-是 */
    private Integer livenessPassed;

    /** 状态：0未提交 1机审通过 2机审失败 3待人工审核 4人工通过 5人工驳回 */
    private Integer status;

    /** 服务商：MOCK / JUMIO / SUM_SUBSTANCE */
    private String provider;

    /** 服务商请求ID */
    private String providerRequestId;

    /** 驳回原因 */
    private String rejectReason;

    /** 审核管理员ID */
    private Long reviewerId;

    /** 审核时间 */
    private LocalDateTime reviewedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
