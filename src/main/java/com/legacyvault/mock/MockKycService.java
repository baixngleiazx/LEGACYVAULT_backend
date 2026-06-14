package com.legacyvault.mock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Mock KYC身份核验服务
 * 模拟第三方KYC核验（Jumio/阿里云实人认证），默认返回核验通过
 *
 * 【Mock模式】当前为模拟实现，所有KYC请求直接返回通过
 * 【切换正式】替换为Jumio SDK 或 阿里云实人认证SDK
 *
 * @author LegacyVault
 */
@Slf4j
@Service
public class MockKycService {

    /**
     * 提交KYC核验请求
     *
     * @param userId   用户ID
     * @param name     真实姓名
     * @param idCardNo 证件号
     * @return 核验结果（Mock直接返回通过）
     */
    public Map<String, Object> submitKyc(Long userId, String name, String idCardNo) {
        log.info("【Mock KYC】提交核验 | 用户ID={} | 姓名={} | 证件号={}", userId, maskName(name), maskIdCard(idCardNo));

        // Mock：直接返回核验通过
        Map<String, Object> result = new HashMap<>();
        result.put("status", "PASSED");
        result.put("kycId", "MOCK_KYC_" + userId + "_" + System.currentTimeMillis());
        result.put("verifiedAt", System.currentTimeMillis());
        result.put("provider", "MOCK");
        result.put("message", "Mock模式：KYC核验自动通过");

        log.info("【Mock KYC】核验通过 | kycId={}", result.get("kycId"));

        /*
         * ========== 正式接口预留（Jumio KYC） ==========
         * 切换步骤：
         * 1. 注册Jumio账号，获取API Key
         * 2. 引入Jumio SDK或调用REST API
         * 3. 创建Session → 用户上传证件照 → 获取核验结果
         * 4. 根据result.status判断是否通过
         *
         * JumioClient client = new JumioClient(apiKey, apiSecret);
         * Session session = client.createSession(userId, name, idCardNo);
         * // 等待用户完成人脸识别...
         * SessionResult result = client.getSessionResult(session.getId());
         */
        return result;
    }

    /**
     * 姓名脱敏
     */
    private String maskName(String name) {
        if (name == null || name.length() <= 1) return name;
        return name.charAt(0) + "**";
    }

    /**
     * 证件号脱敏
     */
    private String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() <= 6) return "***";
        return idCard.substring(0, 3) + "****" + idCard.substring(idCard.length() - 3);
    }
}
