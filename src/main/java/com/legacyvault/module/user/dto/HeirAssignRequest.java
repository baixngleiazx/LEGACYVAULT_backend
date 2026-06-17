package com.legacyvault.module.user.dto;

import lombok.Data;

import java.util.List;

/**
 * 继承人内容分配请求 DTO（支持差异化分配）
 *
 * @author LegacyVault
 */
@Data
public class HeirAssignRequest {

    /** 分配的内容 ID 列表 */
    private List<Long> contentIds;
}
