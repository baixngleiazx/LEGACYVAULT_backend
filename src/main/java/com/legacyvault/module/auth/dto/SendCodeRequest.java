package com.legacyvault.module.auth.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 发送验证码请求DTO
 * target 为手机号或邮箱；channel 为 sms/email（可省略，自动识别）
 */
@Data
public class SendCodeRequest {

    @NotBlank(message = "目标不能为空")
    private String target;

    @NotBlank(message = "类型不能为空")
    private String codeType;  // register/login/heir_confirm/delivery_check

    /** 渠道：email/sms（可选，省略时根据 target 自动识别） */
    private String channel;
}
