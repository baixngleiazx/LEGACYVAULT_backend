package com.legacyvault.module.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.legacyvault.common.Constants;
import com.legacyvault.module.admin.dto.SysConfigVo;
import com.legacyvault.module.admin.entity.SysConfig;
import com.legacyvault.module.admin.mapper.SysConfigMapper;
import com.legacyvault.module.admin.service.SysConfigService;
import com.legacyvault.module.auth.service.AuditLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统配置服务实现
 *
 * @author LegacyVault
 */
@Slf4j
@Service
public class SysConfigServiceImpl implements SysConfigService {

    @Autowired
    private SysConfigMapper sysConfigMapper;

    @Autowired
    private AuditLogService auditLogService;

    @Override
    public List<SysConfigVo> listAll() {
        return sysConfigMapper.selectList(null).stream()
                .map(this::toVo).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdate(Long adminId, List<SysConfigVo> configs) {
        if (configs == null) return;
        for (SysConfigVo vo : configs) {
            SysConfig existing = sysConfigMapper.selectOne(
                    new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, vo.getConfigKey()));
            if (existing != null) {
                existing.setConfigValue(vo.getConfigValue());
                existing.setUpdatedBy(adminId);
                existing.setUpdatedAt(LocalDateTime.now());
                sysConfigMapper.updateById(existing);
            } else {
                SysConfig newConfig = new SysConfig();
                newConfig.setConfigKey(vo.getConfigKey());
                newConfig.setConfigValue(vo.getConfigValue());
                newConfig.setUpdatedBy(adminId);
                newConfig.setUpdatedAt(LocalDateTime.now());
                sysConfigMapper.insert(newConfig);
            }
        }
        auditLogService.log(adminId, Constants.AUDIT_MODULE_SYS_CONFIG, "batch_update",
                String.format("{\"count\":%d}", configs.size()));
    }

    @Override
    public String getValue(String key) {
        SysConfig config = sysConfigMapper.selectOne(
                new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, key));
        return config != null ? config.getConfigValue() : null;
    }

    private SysConfigVo toVo(SysConfig config) {
        SysConfigVo vo = new SysConfigVo();
        vo.setConfigKey(config.getConfigKey());
        vo.setConfigValue(config.getConfigValue());
        vo.setDescription(config.getDescription());
        return vo;
    }
}
