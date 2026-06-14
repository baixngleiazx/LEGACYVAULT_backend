package com.legacyvault.module.user.service;

import com.legacyvault.module.user.dto.TrustedContactRequest;
import com.legacyvault.module.user.entity.TrustedContact;

import java.util.List;

/**
 * 可信联系人服务接口
 *
 * @author LegacyVault
 */
public interface TrustedContactService {

    List<TrustedContact> listContacts(Long userId);

    TrustedContact addContact(Long userId, TrustedContactRequest request);

    void deleteContact(Long userId, Long contactId);
}
