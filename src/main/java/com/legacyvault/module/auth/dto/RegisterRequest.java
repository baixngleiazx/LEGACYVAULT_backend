package com.legacyvault.module.auth.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 用户注册请求DTO
 * 支持邮箱注册和手机号注册（二选一）
 */
@Data
public class RegisterRequest {

    /** 邮箱（手机号注册时可为空） */
    private String email;

    /** 手机号（邮箱注册时可为空） */
    private String phone;

    @NotBlank(message = "验证码不能为空")
    private String verifyCode;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 32, message = "密码长度8-32位")
    private String password;

    /** 昵称（选填） */
    private String nickname;
}
