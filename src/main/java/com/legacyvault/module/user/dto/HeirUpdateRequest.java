package com.legacyvault.module.user.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

/**
 * 编辑继承人请求 DTO
 * 仅当继承人状态为"草稿"或"待确认"时允许编辑
 *
 * @author LegacyVault
 */
@Data
public class HeirUpdateRequest {

    @NotBlank(message = "继承人姓名不能为空")
    private String name;

    @NotBlank(message = "继承人邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    private String phone;

    /** Pro 套餐才允许录入证件号 */
    private String idCardNo;
}
