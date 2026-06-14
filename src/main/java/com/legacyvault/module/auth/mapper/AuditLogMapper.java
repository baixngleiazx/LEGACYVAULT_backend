package com.legacyvault.module.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.legacyvault.module.auth.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作审计日志 Mapper接口
 */
@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {
}
