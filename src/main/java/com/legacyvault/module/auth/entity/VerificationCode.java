package com.legacyvault.module.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 验证码记录实体类
 * 对应表：verification_code
 *
 * @author LegacyVault
 */
@Data
@TableName("verification_code")
public class VerificationCode implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发送目标（邮箱/手机号） */
    private String target;

    /** 验证码 */
    private String code;

    /** 验证码类型 */
    private String codeType;

    /** 渠道：email/sms */
    private String channel;

    /** 是否已使用 */
    private Integer isUsed;

    /** 过期时间 */
    private LocalDateTime expireAt;

    /** Mock发送记录（JSON） */
    private String mockData;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
