package com.legacyvault.module.heartbeat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.legacyvault.module.heartbeat.entity.RemindRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 提醒记录 Mapper接口
 */
@Mapper
public interface RemindRecordMapper extends BaseMapper<RemindRecord> {
}
