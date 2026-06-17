package com.legacyvault.module.registration.dto;

import lombok.Data;

import java.util.List;

/**
 * 5 步注册流程整体状态响应
 *
 * @author LegacyVault
 */
@Data
public class RegistrationStatusResponse {

    /** 当前应该引导用户进入的步骤编号（1-5，全部完成则为 null） */
    private Integer currentStep;

    /** 是否全部完成（含跳过） */
    private Boolean allCompleted;

    /** 每一步的详细状态 */
    private List<StepStatusVo> steps;

    /** 当前套餐 ID */
    private Long planId;

    /** 套餐名 */
    private String planName;

    /** TOTP 是否已绑定（步骤 5 强依赖） */
    private Boolean totpBound;
}
