package com.legacyvault.module.registration.dto;

import lombok.Data;

/**
 * 单步注册步骤状态 VO
 *
 * @author LegacyVault
 */
@Data
public class StepStatusVo {

    /** 步骤编号（1-5） */
    private Integer step;

    /** 步骤名称 */
    private String stepName;

    /** 是否完成：0-否 1-是 */
    private Integer done;

    /** 是否跳过：0-否 1-是 */
    private Integer skipped;

    /** 是否可访问（前端据此判断是否可以进入该步骤页面） */
    private Boolean accessible;

    /** 风险提示文案（跳过时展示） */
    private String skipRiskHint;
}
