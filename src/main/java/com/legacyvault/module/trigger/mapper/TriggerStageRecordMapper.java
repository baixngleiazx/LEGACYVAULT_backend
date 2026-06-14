package com.legacyvault.module.trigger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.legacyvault.module.trigger.entity.TriggerStageRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 触发阶段记录 Mapper接口
 */
@Mapper
public interface TriggerStageRecordMapper extends BaseMapper<TriggerStageRecord> {
}
