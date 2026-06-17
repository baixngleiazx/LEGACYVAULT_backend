package com.legacyvault.module.admin.dto;

import lombok.Data;

import java.util.List;

/**
 * 系统配置批量更新请求
 *
 * @author LegacyVault
 */
@Data
public class SysConfigUpdateRequest {

    private List<SysConfigVo> configs;
}
