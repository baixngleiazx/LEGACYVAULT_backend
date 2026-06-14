package com.legacyvault.module.delivery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.legacyvault.module.delivery.entity.DeliveryAccessLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 交付访问记录 Mapper接口
 */
@Mapper
public interface DeliveryAccessLogMapper extends BaseMapper<DeliveryAccessLog> {
}
