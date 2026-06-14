package com.legacyvault.module.heartbeat.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 心跳打卡记录实体类
 * 对应表：heartbeat_record
 *
 * @author LegacyVault
 */
@Data
@TableName("heartbeat_record")
public class HeartbeatRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 打卡时间 */
    private LocalDateTime checkInAt;

    /** 打卡方式：web/totp/recovery_code */
    private String checkInType;

    /** 打卡IP地址 */
    private String ipAddress;

    /** 设备信息 */
    private String userAgent;

    /** TOTP是否验证通过 */
    private Integer totpVerified;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
