package com.legacyvault.module.kyc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.legacyvault.module.kyc.entity.KycRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * KYC 审核单据 Mapper
 *
 * @author LegacyVault
 */
@Mapper
public interface KycRecordMapper extends BaseMapper<KycRecord> {
}
