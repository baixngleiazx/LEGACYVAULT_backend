package com.legacyvault.module.kyc.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * KYC 提交请求 DTO
 *
 * @author LegacyVault
 */
@Data
public class KycSubmitRequest {

    /** 真实姓名 */
    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    /** 证件类型：ID_CARD / PASSPORT */
    private String idType;

    /** 证件号（明文传输，后端加密存储） */
    @NotBlank(message = "证件号不能为空")
    private String idNo;

    /** 证件正面照 Base64 或 URL */
    private String frontImage;

    /** 证件背面照 */
    private String backImage;

    /** 活体检测结果标识（前端调用活体 SDK 后回传） */
    private Boolean livenessPassed;
}
