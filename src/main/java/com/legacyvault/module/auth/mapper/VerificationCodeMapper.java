package com.legacyvault.module.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.legacyvault.module.auth.entity.VerificationCode;
import org.apache.ibatis.annotations.Mapper;

/**
 * 验证码记录 Mapper接口
 */
@Mapper
public interface VerificationCodeMapper extends BaseMapper<VerificationCode> {
}
