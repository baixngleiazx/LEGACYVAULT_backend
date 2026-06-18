package com.legacyvault.module.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.legacyvault.common.Constants;
import com.legacyvault.common.ResultCode;
import com.legacyvault.exception.BusinessException;
import com.legacyvault.mock.MockStorageService;
import com.legacyvault.module.auth.service.AuditLogService;
import com.legacyvault.module.content.dto.ContentRequest;
import com.legacyvault.module.content.dto.ContentResponse;
import com.legacyvault.module.content.entity.EncryptedContent;
import com.legacyvault.module.content.entity.KeyShard;
import com.legacyvault.module.content.mapper.EncryptedContentMapper;
import com.legacyvault.module.content.mapper.KeyShardMapper;
import com.legacyvault.module.content.service.ContentService;
import com.legacyvault.module.user.entity.User;
import com.legacyvault.module.user.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 加密内容服务实现
 * 管理用户的加密内容（私钥、密码、遗言、文件），包括密钥分片存储
 *
 * @author LegacyVault
 */
@Slf4j
@Service
public class ContentServiceImpl implements ContentService {

    private static final Set<String> SUPPORTED_TYPES = new HashSet<>(Arrays.asList(
            Constants.CONTENT_TYPE_PRIVATE_KEY,
            Constants.CONTENT_TYPE_ACCOUNT_PASSWORD,
            Constants.CONTENT_TYPE_LAST_WORDS,
            Constants.CONTENT_TYPE_FILE
    ));

    private static final int FREE_PRIVATE_KEY_LIMIT = 3;
    private static final int FREE_PASSWORD_LIMIT = 10;
    private static final long FREE_STORAGE_LIMIT_BYTES = 10L * 1024L * 1024L;

    @Autowired
    private EncryptedContentMapper contentMapper;

    @Autowired
    private KeyShardMapper keyShardMapper;

    @Autowired
    private MockStorageService mockStorageService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<ContentResponse> listContents(Long userId, String contentType) {
        LambdaQueryWrapper<EncryptedContent> wrapper = new LambdaQueryWrapper<EncryptedContent>()
                .eq(EncryptedContent::getUserId, userId)
                .eq(EncryptedContent::getStatus, Constants.CONTENT_STATUS_NORMAL);
        if (contentType != null && !contentType.isEmpty()) {
            wrapper.eq(EncryptedContent::getContentType, contentType);
        }
        wrapper.orderByDesc(EncryptedContent::getCreatedAt);

        List<EncryptedContent> contents = contentMapper.selectList(wrapper);
        return contents.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContentResponse createContent(Long userId, ContentRequest request) {
        validateContentRequest(request);
        enforcePlanLimits(userId, request);

        // 存储加密文件（Mock：存本地）
        String storagePath = mockStorageService.uploadEncryptedFile(userId, request.getEncryptedData(), request.getFileName());

        // 创建加密内容记录
        EncryptedContent content = new EncryptedContent();
        content.setUserId(userId);
        content.setContentType(request.getContentType());
        content.setTitle(request.getTitle());
        content.setEncryptedData(request.getEncryptedData());
        content.setContentHash(request.getContentHash());
        content.setFileName(request.getFileName());
        content.setFileSize(request.getFileSize());
        content.setStoragePath(storagePath);
        content.setK2Shard(request.getK2Shard());
        content.setK3Shard(request.getK3Shard());
        content.setStatus(Constants.CONTENT_STATUS_NORMAL);
        contentMapper.insert(content);

        // 存储密钥分片（Shamir 2-of-3）
        if (request.getK2Shard() != null) {
            saveKeyShard(userId, content.getId(), 2, request.getK2Shard(), "hsm");
        }
        if (request.getK3Shard() != null) {
            saveKeyShard(userId, content.getId(), 3, request.getK3Shard(), "third_party");
        }

        auditLogService.log(userId, Constants.AUDIT_MODULE_CONTENT, "create_content",
                String.format("{\"contentId\":%d,\"type\":\"%s\"}", content.getId(), request.getContentType()));
        log.info("加密内容创建成功 | userId={} | contentId={} | type={}", userId, content.getId(), request.getContentType());
        return toResponse(content);
    }

    private void validateContentRequest(ContentRequest request) {
        if (!SUPPORTED_TYPES.contains(request.getContentType())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "不支持的内容类型");
        }
        if (request.getContentHash() == null || !request.getContentHash().matches("^[a-fA-F0-9]{64}$")) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "内容哈希必须为SHA-256十六进制字符串");
        }
        if (request.getK2Shard() == null || request.getK2Shard().trim().isEmpty()
                || request.getK3Shard() == null || request.getK3Shard().trim().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "缺少Shamir托管分片");
        }
        if (Constants.CONTENT_TYPE_FILE.equals(request.getContentType())) {
            if (request.getFileName() == null || request.getFileName().trim().isEmpty()) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "文件内容必须包含文件名");
            }
            if (request.getFileSize() == null || request.getFileSize() <= 0) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "文件大小无效");
            }
        }
    }

    private void enforcePlanLimits(Long userId, ContentRequest request) {
        User user = userMapper.selectById(userId);
        boolean freePlan = user == null || user.getPlanId() == null || user.getPlanId() == 1L;
        if (!freePlan) {
            return;
        }

        Long typeCount = contentMapper.selectCount(
                new LambdaQueryWrapper<EncryptedContent>()
                        .eq(EncryptedContent::getUserId, userId)
                        .eq(EncryptedContent::getContentType, request.getContentType())
                        .eq(EncryptedContent::getStatus, Constants.CONTENT_STATUS_NORMAL));

        if (Constants.CONTENT_TYPE_PRIVATE_KEY.equals(request.getContentType()) && typeCount >= FREE_PRIVATE_KEY_LIMIT) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "Free套餐最多保存3条私钥");
        }
        if (Constants.CONTENT_TYPE_ACCOUNT_PASSWORD.equals(request.getContentType()) && typeCount >= FREE_PASSWORD_LIMIT) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "Free套餐最多保存10条账户密码");
        }
        long currentBytes = contentMapper.selectList(
                new LambdaQueryWrapper<EncryptedContent>()
                        .eq(EncryptedContent::getUserId, userId)
                        .eq(EncryptedContent::getStatus, Constants.CONTENT_STATUS_NORMAL))
                .stream()
                .mapToLong(item -> item.getFileSize() == null ? 0L : item.getFileSize())
                .sum();
        long incomingBytes = request.getFileSize() == null ? 0L : request.getFileSize();
        if (currentBytes + incomingBytes > FREE_STORAGE_LIMIT_BYTES) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "Free套餐总存储上限为10MB");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteContent(Long userId, Long contentId) {
        EncryptedContent content = contentMapper.selectById(contentId);
        if (content == null || !content.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.CONTENT_NOT_FOUND);
        }
        content.setStatus(Constants.CONTENT_STATUS_DELETED);
        contentMapper.updateById(content);

        // 删除关联的分片
        keyShardMapper.delete(new LambdaQueryWrapper<KeyShard>().eq(KeyShard::getContentId, contentId));

        auditLogService.log(userId, Constants.AUDIT_MODULE_CONTENT, "delete_content",
                String.format("{\"contentId\":%d}", contentId));
    }

    @Override
    public ContentResponse getContentDetail(Long userId, Long contentId) {
        EncryptedContent content = contentMapper.selectById(contentId);
        if (content == null || !content.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.CONTENT_NOT_FOUND);
        }
        ContentResponse response = toResponse(content);
        response.setEncryptedData(content.getEncryptedData());
        response.setK2Shard(content.getK2Shard());
        return response;
    }

    /**
     * 保存密钥分片
     */
    private void saveKeyShard(Long userId, Long contentId, int index, String shardData, String location) {
        KeyShard shard = new KeyShard();
        shard.setUserId(userId);
        shard.setContentId(contentId);
        shard.setShardIndex(index);
        shard.setShardData(shardData);
        shard.setStorageLocation(location);
        keyShardMapper.insert(shard);
    }

    /**
     * 实体转响应VO
     */
    private ContentResponse toResponse(EncryptedContent content) {
        ContentResponse response = new ContentResponse();
        response.setId(content.getId());
        response.setContentType(content.getContentType());
        String[] typeNames = {"私钥", "账户密码", "遗言", "文件"};
        String[] types = {Constants.CONTENT_TYPE_PRIVATE_KEY, Constants.CONTENT_TYPE_ACCOUNT_PASSWORD,
                Constants.CONTENT_TYPE_LAST_WORDS, Constants.CONTENT_TYPE_FILE};
        for (int i = 0; i < types.length; i++) {
            if (types[i].equals(content.getContentType())) {
                response.setContentTypeText(typeNames[i]);
                break;
            }
        }
        response.setTitle(content.getTitle());
        response.setContentHash(content.getContentHash());
        response.setFileName(content.getFileName());
        response.setFileSize(content.getFileSize());
        response.setStatus(content.getStatus());
        // 查询分片数量
        Long shardCount = keyShardMapper.selectCount(
                new LambdaQueryWrapper<KeyShard>().eq(KeyShard::getContentId, content.getId()));
        response.setShardCount(shardCount.intValue());
        response.setCreatedAt(content.getCreatedAt());
        response.setUpdatedAt(content.getUpdatedAt());
        return response;
    }
}
