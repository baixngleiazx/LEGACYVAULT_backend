package com.legacyvault.module.heartbeat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.legacyvault.module.heartbeat.entity.HeartbeatRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 心跳打卡记录 Mapper接口
 */
@Mapper
public interface HeartbeatRecordMapper extends BaseMapper<HeartbeatRecord> {
}
