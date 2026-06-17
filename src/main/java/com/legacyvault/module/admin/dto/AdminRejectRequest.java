package com.legacyvault.module.admin.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * KYC 驳回请求
 *
 * @author LegacyVault
 */
@Data
public class AdminRejectRequest {

    @NotBlank(message = "驳回原因不能为空")
    private String rejectReason;
}
