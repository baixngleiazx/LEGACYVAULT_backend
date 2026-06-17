package com.legacyvault.module.biometric.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.legacyvault.module.biometric.entity.BiometricRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 生物特征标识 Mapper
 *
 * @author LegacyVault
 */
@Mapper
public interface BiometricRecordMapper extends BaseMapper<BiometricRecord> {
}
