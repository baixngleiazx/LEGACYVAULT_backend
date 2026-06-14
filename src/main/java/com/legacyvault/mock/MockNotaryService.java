package com.legacyvault.mock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Mock公证服务
 * 模拟数字公证接口（新加坡数字公证/法大大/Notarize）
 *
 * 【Mock模式】当前为模拟实现，直接返回公证成功
 * 【切换正式】替换为对应司法区的公证服务API
 *
 * @author LegacyVault
 */
@Slf4j
@Service
public class MockNotaryService {

    /**
     * 发送公证人通知（触发流程T+96h阶段）
     *
     * @param userId  用户ID
     * @param processId 触发流程ID
     * @return 通知结果
     */
    public Map<String, Object> notifyNotary(Long userId, Long processId) {
        log.info("【Mock公证】通知公证人 | 用户ID={} | 流程ID={}", userId, processId);

        Map<String, Object> result = new HashMap<>();
        result.put("notified", true);
        result.put("notaryId", "MOCK_NOTARY_" + processId);
        result.put("jurisdiction", "SG-MOCK");
        result.put("message", "Mock模式：公证人通知已发送");

        log.info("【Mock公证】通知成功 | notaryId={}", result.get("notaryId"));

        /*
         * ========== 正式接口预留 ==========
         * 新加坡数字公证：
         *   调用 SingaporeNotary API → POST /api/v1/notifications
         * 中国法大大/e签宝：
         *   调用 Fadada API → 实名认证 + 存证出证
         * 美国Notarize：
         *   调用 Notarize API → POST /v2/sessions
         */
        return result;
    }

    /**
     * 生成数字资产意图声明书
     *
     * @param userId 用户ID
     * @return 声明书信息
     */
    public Map<String, Object> generateIntentDeclaration(Long userId) {
        log.info("【Mock公证】生成意图声明书 | 用户ID={}", userId);

        Map<String, Object> result = new HashMap<>();
        result.put("documentId", "MOCK_DOC_" + userId);
        result.put("status", "GENERATED");
        result.put("downloadUrl", "/mock/declaration/" + userId);
        result.put("message", "Mock模式：声明书已生成");
        return result;
    }
}
