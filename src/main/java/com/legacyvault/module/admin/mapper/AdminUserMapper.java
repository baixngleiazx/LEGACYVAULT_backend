package com.legacyvault.module.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.legacyvault.module.admin.entity.AdminUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 管理员账号 Mapper
 *
 * @author LegacyVault
 */
@Mapper
public interface AdminUserMapper extends BaseMapper<AdminUser> {
}
