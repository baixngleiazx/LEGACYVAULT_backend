package com.legacyvault.module.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.legacyvault.common.Constants;
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
            trustedContactMapper.deleteById(contactId);
        }
    }
}
