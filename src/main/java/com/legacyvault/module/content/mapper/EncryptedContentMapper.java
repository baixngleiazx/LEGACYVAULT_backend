package com.legacyvault.module.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.legacyvault.module.content.entity.EncryptedContent;
import org.apache.ibatis.annotations.Mapper;

/**
 * 加密内容 Mapper接口
 */
@Mapper
public interface EncryptedContentMapper extends BaseMapper<EncryptedContent> {
}
