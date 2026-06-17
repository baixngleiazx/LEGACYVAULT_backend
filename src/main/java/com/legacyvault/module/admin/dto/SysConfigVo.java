package com.legacyvault.module.admin.dto;

import lombok.Data;

/**
 * 系统配置 VO
 *
 * @author LegacyVault
 */
@Data
public class SysConfigVo {

    private String configKey;
    private String configValue;
    private String description;
}
