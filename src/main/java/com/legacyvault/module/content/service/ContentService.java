package com.legacyvault.module.content.service;

import com.legacyvault.module.content.dto.ContentRequest;
import com.legacyvault.module.content.dto.ContentResponse;

import java.util.List;

/**
 * 加密内容服务接口
 *
 * @author LegacyVault
 */
public interface ContentService {

    List<ContentResponse> listContents(Long userId, String contentType);

    ContentResponse createContent(Long userId, ContentRequest request);

    void deleteContent(Long userId, Long contentId);

    ContentResponse getContentDetail(Long userId, Long contentId);
}
