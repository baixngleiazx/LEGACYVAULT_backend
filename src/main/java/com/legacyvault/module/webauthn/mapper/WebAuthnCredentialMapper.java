package com.legacyvault.module.webauthn.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.legacyvault.module.webauthn.entity.WebAuthnCredential;
import org.apache.ibatis.annotations.Mapper;

/**
 * WebAuthn 凭证 Mapper
 *
 * @author LegacyVault
 */
@Mapper
public interface WebAuthnCredentialMapper extends BaseMapper<WebAuthnCredential> {
}
