package com.legacyvault.mock;

import com.legacyvault.config.LegacyVaultProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * KYC 身份核验服务
 * Mock 模式：直接返回核验通过
 * 真实模式：调用 Jumio / Sum&Substance SDK（待接入）
 *
 * 双分支逻辑通过 LegacyVaultProperties.mockModeEnabled 控制
 *
 * @author LegacyVault
 */
@Slf4j
@Service
public class MockKycService {

    @Autowired
    private LegacyVaultProperties properties;

    /**
     * 提交 KYC 核验请求
     *
     * @param userId   用户ID
     * @param name     真实姓名
     * @param idCardNo 证件号
     * @return 核验结果
     */
    public Map<String, Object> submitKyc(Long userId, String name, String idCardNo) {
        if (Boolean.TRUE.equals(properties.getMockModeEnabled())) {
            return submitKycMock(userId, name, idCardNo);
        }
        return submitKycReal(userId, name, idCardNo);
    }

    /**
     * 【Mock 分支】直接返回通过
     */
    private Map<String, Object> submitKycMock(Long userId, String name, String idCardNo) {
        log.info("【Mock KYC】提交核验 | 用户ID={} | 姓名={} | 证件号={}", userId, maskName(name), maskIdCard(idCardNo));

        Map<String, Object> result = new HashMap<>();
        result.put("status", "PASSED");
        result.put("kycId", "MOCK_KYC_" + userId + "_" + System.currentTimeMillis());
        result.put("verifiedAt", System.currentTimeMillis());
        result.put("provider", "MOCK");
        result.put("message", "Mock模式：KYC核验自动通过");

        log.info("【Mock KYC】核验通过 | kycId={}", result.get("kycId"));
        return result;
    }

    /**
     * 【真实分支】调用第三方 KYC SDK（Jumio / Sum&Substance）
     * 当前为占位实现，抛出异常提示未接入
     */
    private Map<String, Object> submitKycReal(Long userId, String name, String idCardNo) {
        log.info("【真实 KYC】提交核验 | 用户ID={} | 姓名={}", userId, maskName(name));

        /*
         * ========== 真实接口预留（Jumio KYC） ==========
         * JumioClient client = new JumioClient(apiKey, apiSecret);
         * Session session = client.createSession(userId, name, idCardNo);
         * SessionResult result = client.getSessionResult(session.getId());
         * Map<String, Object> out = new HashMap<>();
         * out.put("status", result.isPassed() ? "PASSED" : "FAILED");
         * out.put("kycId", result.getId());
         * return out;
         */
        Map<String, Object> result = new HashMap<>();
        result.put("status", "FAILED");
        result.put("message", "真实 KYC 服务商暂未接入，请联系管理员配置 Mock 模式");
        return result;
    }

    private String maskName(String name) {
        if (name == null || name.length() <= 1) return name;
        return name.charAt(0) + "**";
    }

    private String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() <= 6) return "***";
        return idCard.substring(0, 3) + "****" + idCard.substring(idCard.length() - 3);
    }
}
