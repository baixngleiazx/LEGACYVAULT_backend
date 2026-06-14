package com.legacyvault.module.user.dto;

import lombok.Data;
import javax.validation.constraints.*;

/**
 * 添加可信联系人请求DTO
 */
@Data
public class TrustedContactRequest {
    @NotBlank(message = "联系人姓名不能为空")
    private String name;

    @NotBlank(message = "联系人邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    private String phone;
    private String relationship;
}
