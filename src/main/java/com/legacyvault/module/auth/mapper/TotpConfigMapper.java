package com.legacyvault.module.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.legacyvault.module.auth.entity.TotpConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * TOTP配置 Mapper接口
 */
@Mapper
public interface TotpConfigMapper extends BaseMapper<TotpConfig> {
}
