package com.legacyvault.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.legacyvault.module.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper接口
 *
 * @author LegacyVault
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
