package com.legacyvault.module.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.legacyvault.common.Constants;
import com.legacyvault.common.ResultCode;
import com.legacyvault.exception.BusinessException;
import com.legacyvault.mock.MockEmailService;
import com.legacyvault.module.auth.service.AuditLogService;
import com.legacyvault.module.user.dto.HeirRequest;
import com.legacyvault.module.user.dto.HeirResponse;
import com.legacyvault.module.user.entity.Heir;
import com.legacyvault.module.user.mapper.HeirMapper;
import com.legacyvault.module.user.service.HeirService;
import com.legacyvault.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 继承人服务实现
 *
 * @author LegacyVault
 */
@Slf4j
@Service
public class HeirServiceImpl implements HeirService {

    @Autowired
    private HeirMapper heirMapper;

    @Autowired
    private MockEmailService mockEmailService;

    @Autowired
    private AuditLogService auditLogService;

    @Override
    public List<HeirResponse> listHeirs(Long userId) {
        List<Heir> heirs = heirMapper.selectList(
                new LambdaQueryWrapper<Heir>().eq(Heir::getUserId, userId).orderByAsc(Heir::getSortOrder));
        return heirs.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HeirResponse addHeir(Long userId, HeirRequest request) {
        // 检查继承人数量限制（Free套餐1人，Pro 5人，Vault无限）
        Long count = heirMapper.selectCount(
                new LambdaQueryWrapper<Heir>().eq(Heir::getUserId, userId));
        // 简化处理：默认限制10人
        if (count >= 10) {
            throw new BusinessException(ResultCode.HEIR_LIMIT_EXCEEDED);
        }

        Heir heir = new Heir();
        heir.setUserId(userId);
        heir.setName(request.getName());
        heir.setEmail(request.getEmail());
        heir.setPhone(request.getPhone());
        heir.setIdCardNo(request.getIdCardNo());
        heir.setConfirmationStatus(Constants.HEIR_STATUS_PENDING);
        heir.setConfirmationToken(SecurityUtil.generateToken());
        heir.setAssignedContentCount(0);
        heir.setSortOrder(count.intValue());
        heirMapper.insert(heir);

        // 发送确认邀请邮件
        String confirmUrl = "/heir/confirm?token=" + heir.getConfirmationToken();
        mockEmailService.sendHeirConfirmInvite(request.getEmail(), "用户", confirmUrl);

        auditLogService.log(userId, Constants.AUDIT_MODULE_HEIR, "add_heir",
                String.format("{\"heirId\":%d,\"email\":\"%s\"}", heir.getId(), request.getEmail()));
        return toResponse(heir);
    }

    @Override
    public void deleteHeir(Long userId, Long heirId) {
        Heir heir = heirMapper.selectById(heirId);
        if (heir == null || !heir.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.HEIR_NOT_FOUND);
        }
        heirMapper.deleteById(heirId);
        auditLogService.log(userId, Constants.AUDIT_MODULE_HEIR, "delete_heir",
                String.format("{\"heirId\":%d}", heirId));
    }

    @Override
    public void confirmHeirInvite(String token) {
        Heir heir = heirMapper.selectOne(
                new LambdaQueryWrapper<Heir>().eq(Heir::getConfirmationToken, token));
        if (heir == null) {
            throw new BusinessException(ResultCode.HEIR_CONFIRM_TOKEN_INVALID);
        }
        if (heir.getConfirmationStatus() == Constants.HEIR_STATUS_CONFIRMED) {
            throw new BusinessException(ResultCode.HEIR_ALREADY_CONFIRMED);
        }

        heir.setConfirmationStatus(Constants.HEIR_STATUS_CONFIRMED);
        heir.setConfirmedAt(java.time.LocalDateTime.now());
        heirMapper.updateById(heir);
        log.info("继承人确认邀请 | heirId={}", heir.getId());
    }

    @Override
    public void assignContent(Long userId, Long heirId, List<Long> contentIds) {
        Heir heir = heirMapper.selectById(heirId);
        if (heir == null || !heir.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.HEIR_NOT_FOUND);
        }
        heir.setAssignedContentCount(contentIds.size());
        heirMapper.updateById(heir);
        auditLogService.log(userId, Constants.AUDIT_MODULE_HEIR, "assign_content",
                String.format("{\"heirId\":%d,\"contentCount\":%d}", heirId, contentIds.size()));
    }

    /**
     * 实体转响应VO
     */
    private HeirResponse toResponse(Heir heir) {
        HeirResponse response = new HeirResponse();
        response.setId(heir.getId());
        response.setName(heir.getName());
        response.setEmail(heir.getEmail());
        response.setPhone(heir.getPhone());
        response.setConfirmationStatus(heir.getConfirmationStatus());
        String[] statusTexts = {"待确认", "已确认", "已拒绝"};
        response.setConfirmationStatusText(statusTexts[heir.getConfirmationStatus()]);
        response.setAssignedContentCount(heir.getAssignedContentCount());
        response.setConfirmedAt(heir.getConfirmedAt());
        response.setCreatedAt(heir.getCreatedAt());
        return response;
    }
}
