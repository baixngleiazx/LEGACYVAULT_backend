package com.legacyvault.module.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.legacyvault.common.Constants;
import com.legacyvault.common.ResultCode;
import com.legacyvault.exception.BusinessException;
import com.legacyvault.module.auth.service.AuditLogService;
import com.legacyvault.module.user.dto.TrustedContactRequest;
import com.legacyvault.module.user.entity.TrustedContact;
import com.legacyvault.module.user.mapper.TrustedContactMapper;
import com.legacyvault.module.user.service.TrustedContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 可信联系人服务实现
 *
 * @author LegacyVault
 */
@Service
public class TrustedContactServiceImpl implements TrustedContactService {

    private static final int MIN_CONTACTS = 3;
    private static final int MAX_CONTACTS = 5;

    @Autowired
    private TrustedContactMapper trustedContactMapper;

    @Autowired
    private AuditLogService auditLogService;

    @Override
    public List<TrustedContact> listContacts(Long userId) {
        return trustedContactMapper.selectList(
                new LambdaQueryWrapper<TrustedContact>().eq(TrustedContact::getUserId, userId));
    }

    @Override
    public TrustedContact addContact(Long userId, TrustedContactRequest request) {
        Long count = trustedContactMapper.selectCount(
                new LambdaQueryWrapper<TrustedContact>().eq(TrustedContact::getUserId, userId));
        if (count >= MAX_CONTACTS) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "可信联系人最多配置5人");
        }
        Long duplicateCount = trustedContactMapper.selectCount(
                new LambdaQueryWrapper<TrustedContact>()
                        .eq(TrustedContact::getUserId, userId)
                        .eq(TrustedContact::getEmail, request.getEmail()));
        if (duplicateCount > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "该可信联系人邮箱已存在");
        }
        TrustedContact contact = new TrustedContact();
        contact.setUserId(userId);
        contact.setName(request.getName());
        contact.setEmail(request.getEmail());
        contact.setPhone(request.getPhone());
        contact.setRelationship(request.getRelationship());
        trustedContactMapper.insert(contact);

        auditLogService.log(userId, "user", "add_trusted_contact",
                String.format("{\"contactId\":%d}", contact.getId()));
        return contact;
    }

    @Override
    public void deleteContact(Long userId, Long contactId) {
        TrustedContact contact = trustedContactMapper.selectById(contactId);
        if (contact != null && contact.getUserId().equals(userId)) {
            Long count = trustedContactMapper.selectCount(
                    new LambdaQueryWrapper<TrustedContact>().eq(TrustedContact::getUserId, userId));
            if (count <= MIN_CONTACTS) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "可信联系人至少保留3人");
            }
            trustedContactMapper.deleteById(contactId);
        }
    }
}
