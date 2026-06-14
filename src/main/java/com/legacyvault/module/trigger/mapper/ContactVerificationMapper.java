package com.legacyvault.module.trigger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.legacyvault.module.trigger.entity.ContactVerification;
import org.apache.ibatis.annotations.Mapper;

/**
 * 联系人核查记录 Mapper接口
 */
@Mapper
public interface ContactVerificationMapper extends BaseMapper<ContactVerification> {
}
