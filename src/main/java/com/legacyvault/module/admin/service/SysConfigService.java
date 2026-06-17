package com.legacyvault.module.admin.service;

import com.legacyvault.module.admin.dto.SysConfigVo;

import java.util.List;

/**
 * 系统配置服务接口
 *
 * @author LegacyVault
 */
public interface SysConfigService {

    /**
     * 查询全部系统配置
     */
    List<SysConfigVo> listAll();

    /**
     * 批量更新系统配置
     */
    void batchUpdate(Long adminId, List<SysConfigVo> configs);

    /**
     * 根据 key 读取配置值
     */
    String getValue(String key);
}
