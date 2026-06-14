package com.legacyvault.module.user.dto;

import lombok.Data;
import javax.validation.constraints.*;

/**
 * 添加/更新继承人请求DTO
 */
@Data
public class HeirRequest {
    @NotBlank(message = "继承人姓名不能为空")
    private String name;

    @NotBlank(message = "继承人邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    private String phone;
    private String idCardNo;
}
