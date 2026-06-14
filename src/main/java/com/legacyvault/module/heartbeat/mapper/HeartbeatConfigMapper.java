package com.legacyvault.module.heartbeat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.legacyvault.module.heartbeat.entity.HeartbeatConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 心跳配置 Mapper接口
 */
@Mapper
public interface HeartbeatConfigMapper extends BaseMapper<HeartbeatConfig> {
}
