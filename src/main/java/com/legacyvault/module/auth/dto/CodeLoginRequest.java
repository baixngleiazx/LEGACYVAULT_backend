package com.legacyvault.module.auth.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 验证码登录请求DTO
 * 支持手机号/邮箱 + 验证码登录
 */
@Data
public class CodeLoginRequest {

    /** 手机号或邮箱 */
    @NotBlank(message = "手机号或邮箱不能为空")
    private String target;

    /** 验证码 */
    @NotBlank(message = "验证码不能为空")
    private String verifyCode;
}
