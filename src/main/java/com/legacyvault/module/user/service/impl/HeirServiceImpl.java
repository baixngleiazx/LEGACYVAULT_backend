package com.legacyvault.module.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.legacyvault.common.Constants;
import com.legacyvault.common.ResultCode;
import com.legacyvault.exception.BusinessException;
import com.legacyvault.mock.MockEmailService;
import com.legacyvault.module.auth.service.AuditLogService;
import com.legacyvault.module.user.dto.*;
import com.legacyvault.module.user.entity.Heir;
import com.legacyvault.module.user.entity.HeirContentAssignment;
import com.legacyvault.module.user.entity.User;
import com.legacyvault.module.user.mapper.HeirContentAssignmentMapper;
import com.legacyvault.module.user.mapper.HeirMapper;
import com.legacyvault.module.user.mapper.UserMapper;
import com.legacyvault.module.user.service.HeirService;
import com.legacyvault.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
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
    private HeirContentAssignmentMapper assignmentMapper;

    @Autowired
    private UserMapper userMapper;

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
        User user = userMapper.selectById(userId);
        checkHeirLimit(user);

        Long count = heirMapper.selectCount(
                new LambdaQueryWrapper<Heir>().eq(Heir::getUserId, userId));

        Heir heir = new Heir();
        heir.setUserId(userId);
        heir.setName(request.getName());
        heir.setEmail(request.getEmail());
        heir.setPhone(request.getPhone());
        heir.setIdNoEncrypted(encryptIdCard(request.getIdCardNo(), user));
        heir.setConfirmationStatus(Constants.HEIR_STATUS_PENDING); // 待确认
        heir.setStatus(Constants.HEIR_STATUS_PENDING); // 已发邀请
        heir.setConfirmationToken(SecurityUtil.generateToken());
        heir.setInvitedAt(LocalDateTime.now());
        heir.setLastInviteSentAt(LocalDateTime.now());
        heir.setAssignedContentCount(0);
        heir.setSortOrder(count.intValue());
        heirMapper.insert(heir);

        // 发送确认邀请邮件
        sendInviteEmail(heir, user.getNickname());

        auditLogService.log(userId, Constants.AUDIT_MODULE_HEIR, "add_heir",
                String.format("{\"heirId\":%d,\"email\":\"%s\"}", heir.getId(), request.getEmail()));
        return toResponse(heir);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HeirResponse updateHeir(Long userId, Long heirId, HeirUpdateRequest request) {
        Heir heir = loadAndCheckOwnership(userId, heirId);
        // 仅草稿/待确认状态允许编辑
        if (heir.getStatus() != null && heir.getStatus() == Constants.HEIR_STATUS_CONFIRMED) {
            throw new BusinessException("继承人已确认，不允许编辑");
        }

        User user = userMapper.selectById(userId);
        heir.setName(request.getName());
        heir.setEmail(request.getEmail());
        heir.setPhone(request.getPhone());
        heir.setIdNoEncrypted(encryptIdCard(request.getIdCardNo(), user));
        heirMapper.updateById(heir);

        auditLogService.log(userId, Constants.AUDIT_MODULE_HEIR, "update_heir",
                String.format("{\"heirId\":%d}", heirId));
        return toResponse(heir);
    }

    @Override
    public void deleteHeir(Long userId, Long heirId) {
        Heir heir = loadAndCheckOwnership(userId, heirId);
        // 已确认的继承人不允许删除（需先解除确认）
        if (heir.getStatus() != null && heir.getStatus() == Constants.HEIR_STATUS_CONFIRMED) {
            throw new BusinessException("继承人已确认，不允许直接删除");
        }
        heirMapper.deleteById(heirId);
        // 清理分配明细
        assignmentMapper.delete(
                new LambdaQueryWrapper<HeirContentAssignment>().eq(HeirContentAssignment::getHeirId, heirId));
        auditLogService.log(userId, Constants.AUDIT_MODULE_HEIR, "delete_heir",
                String.format("{\"heirId\":%d}", heirId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resendInvite(Long userId, Long heirId) {
        Heir heir = loadAndCheckOwnership(userId, heirId);
        if (heir.getStatus() == null || heir.getStatus() != Constants.HEIR_STATUS_PENDING) {
            throw new BusinessException("仅「待确认」状态可重发邀请");
        }
        // 24 小时冷却
        if (heir.getLastInviteSentAt() != null) {
            long hours = Duration.between(heir.getLastInviteSentAt(), LocalDateTime.now()).toHours();
            if (hours < 24) {
                throw new BusinessException(String.format("邀请发送冷却中，请 %d 小时后再试", 24 - hours));
            }
        }

        // 重新生成 token
        heir.setConfirmationToken(SecurityUtil.generateToken());
        heir.setLastInviteSentAt(LocalDateTime.now());
        heirMapper.updateById(heir);

        User user = userMapper.selectById(userId);
        sendInviteEmail(heir, user.getNickname());

        auditLogService.log(userId, Constants.AUDIT_MODULE_HEIR, "resend_invite",
                String.format("{\"heirId\":%d}", heirId));
    }

    @Override
    public void confirmHeirInvite(String token) {
        Heir heir = heirMapper.selectOne(
                new LambdaQueryWrapper<Heir>().eq(Heir::getConfirmationToken, token));
        if (heir == null) {
            throw new BusinessException(ResultCode.HEIR_CONFIRM_TOKEN_INVALID);
        }
        if (heir.getConfirmationStatus() != null
                && heir.getConfirmationStatus() == Constants.HEIR_STATUS_CONFIRMED) {
            throw new BusinessException(ResultCode.HEIR_ALREADY_CONFIRMED);
        }

        heir.setConfirmationStatus(Constants.HEIR_STATUS_CONFIRMED);
        heir.setStatus(Constants.HEIR_STATUS_CONFIRMED);
        heir.setConfirmedAt(LocalDateTime.now());
        heirMapper.updateById(heir);
        log.info("继承人确认邀请 | heirId={}", heir.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignContent(Long userId, Long heirId, HeirAssignRequest request) {
        Heir heir = loadAndCheckOwnership(userId, heirId);
        List<Long> contentIds = request.getContentIds();
        if (contentIds == null) contentIds = java.util.Collections.emptyList();

        // 先清空原分配
        assignmentMapper.delete(
                new LambdaQueryWrapper<HeirContentAssignment>().eq(HeirContentAssignment::getHeirId, heirId));

        // 写入新分配
        LocalDateTime now = LocalDateTime.now();
        for (Long contentId : contentIds) {
            HeirContentAssignment a = new HeirContentAssignment();
            a.setHeirId(heirId);
            a.setContentId(contentId);
            a.setAssignedAt(now);
            assignmentMapper.insert(a);
        }

        heir.setAssignedContentCount(contentIds.size());
        heirMapper.updateById(heir);

        auditLogService.log(userId, Constants.AUDIT_MODULE_HEIR, "assign_content",
                String.format("{\"heirId\":%d,\"contentCount\":%d}", heirId, contentIds.size()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setUnlockThreshold(Long userId, UnlockThresholdRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        Integer min = request.getMinHeirsToUnlock();
        // 上限 = 当前已确认继承人数
        Long confirmedCount = heirMapper.selectCount(
                new LambdaQueryWrapper<Heir>()
                        .eq(Heir::getUserId, userId)
                        .eq(Heir::getStatus, Constants.HEIR_STATUS_CONFIRMED));
        if (min > confirmedCount.intValue()) {
            throw new BusinessException("解锁门槛不得超过已确认继承人数");
        }
        user.setMinHeirsToUnlock(min);
        userMapper.updateById(user);

        auditLogService.log(userId, Constants.AUDIT_MODULE_HEIR, "set_unlock_threshold",
                String.format("{\"minHeirsToUnlock\":%d}", min));
    }

    @Override
    public Integer getUnlockThreshold(Long userId) {
        User user = userMapper.selectById(userId);
        return user != null ? (user.getMinHeirsToUnlock() != null ? user.getMinHeirsToUnlock() : 1) : 1;
    }

    // ===== 私有工具方法 =====

    private Heir loadAndCheckOwnership(Long userId, Long heirId) {
        Heir heir = heirMapper.selectById(heirId);
        if (heir == null || !heir.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.HEIR_NOT_FOUND);
        }
        return heir;
    }

    private void checkHeirLimit(User user) {
        Long count = heirMapper.selectCount(
                new LambdaQueryWrapper<Heir>().eq(Heir::getUserId, user.getId()));
        int limit = user.getPlanHeirLimit() != null ? user.getPlanHeirLimit() : Constants.HEIR_LIMIT_FREE;
        if (count >= limit) {
            throw new BusinessException(ResultCode.HEIR_LIMIT_EXCEEDED,
                    String.format("当前套餐最多绑定 %d 名继承人", limit));
        }
    }

    private String encryptIdCard(String idCardNo, User user) {
        if (idCardNo == null || idCardNo.isEmpty()) return null;
        // 仅 Pro/Vault 套餐允许录入证件号
        if (user.getPlanId() == null || user.getPlanId() < Constants.PLAN_PRO) {
            return null;
        }
        // 简化加密：SHA-256（真实环境用 AES）
        return SecurityUtil.sha256(idCardNo);
    }

    private void sendInviteEmail(Heir heir, String userName) {
        String confirmUrl = "/heir/confirm?token=" + heir.getConfirmationToken();
        mockEmailService.sendHeirConfirmInvite(heir.getEmail(), userName, confirmUrl);
    }

    private HeirResponse toResponse(Heir heir) {
        HeirResponse response = new HeirResponse();
        response.setId(heir.getId());
        response.setName(heir.getName());
        response.setEmail(heir.getEmail());
        response.setPhone(heir.getPhone());
        response.setConfirmationStatus(heir.getConfirmationStatus());
        response.setStatus(heir.getStatus());
        response.setConfirmationStatusText(resolveStatusText(heir.getStatus() != null ? heir.getStatus() : 0));
        response.setAssignedContentCount(heir.getAssignedContentCount());
        // 查询分配的具体内容 ID
        List<HeirContentAssignment> assignments = assignmentMapper.selectList(
                new LambdaQueryWrapper<HeirContentAssignment>().eq(HeirContentAssignment::getHeirId, heir.getId()));
        response.setAssignedContentIds(
                assignments.stream().map(HeirContentAssignment::getContentId).collect(Collectors.toList()));
        response.setInvitedAt(heir.getInvitedAt());
        response.setLastInviteSentAt(heir.getLastInviteSentAt());
        response.setConfirmedAt(heir.getConfirmedAt());
        response.setCreatedAt(heir.getCreatedAt());
        return response;
    }

    private String resolveStatusText(Integer status) {
        switch (status) {
            case Constants.HEIR_STATUS_DRAFT: return "草稿";
            case Constants.HEIR_STATUS_PENDING: return "待确认";
            case Constants.HEIR_STATUS_CONFIRMED: return "已确认";
            case Constants.HEIR_STATUS_REJECTED: return "已拒绝";
            default: return "未知";
        }
    }
}
