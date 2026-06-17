package com.legacyvault.module.biometric.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 生物特征标识实体
 * 对应表：biometric_record（仅存加密特征标识，不存原始图像）
 *
 * @author LegacyVault
 */
@Data
@TableName("biometric_record")
public class BiometricRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户ID */
    private Long userId;

    /** 生物类型：FACE / FINGER */
    private String biometricType;

    /** 加密特征标识（哈希值） */
    private String featureHash;

    /** 设备信息 */
    private String deviceInfo;

    /** 绑定时间 */
    private LocalDateTime boundAt;
}
