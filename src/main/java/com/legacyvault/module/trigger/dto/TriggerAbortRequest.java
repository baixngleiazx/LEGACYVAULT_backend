package com.legacyvault.module.trigger.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 用户主动中止触发流程请求。
 */
@Data
public class TriggerAbortRequest {

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "TOTP验证码不能为空")
    private String totpCode;
}
