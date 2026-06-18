package com.legacyvault.module.auth.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 绑定邮箱/手机号请求。
 */
@Data
public class BindContactRequest {

    @NotBlank(message = "绑定目标不能为空")
    private String target;

    @NotBlank(message = "验证码不能为空")
    private String verifyCode;
}
