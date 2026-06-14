package com.legacyvault.module.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.legacyvault.module.auth.entity.RecoveryCode;
import org.apache.ibatis.annotations.Mapper;

/**
 * 紧急恢复码 Mapper接口
 */
@Mapper
public interface RecoveryCodeMapper extends BaseMapper<RecoveryCode> {
}
