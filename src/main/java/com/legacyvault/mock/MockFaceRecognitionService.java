package com.legacyvault.mock;

import com.legacyvault.config.LegacyVaultProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 人脸识别服务（Mock / 真实双分支）
 *
 * @author LegacyVault
 */
@Slf4j
@Service
public class MockFaceRecognitionService {

    @Autowired
    private LegacyVaultProperties properties;

    public Map<String, Object> verifyFace(Long userId, Long heirId, String faceImageBase64) {
        if (Boolean.TRUE.equals(properties.getMockModeEnabled())) {
            return verifyFaceMock(userId, heirId);
        }
        return verifyFaceReal(userId, heirId);
    }

    public Map<String, Object> registerFace(Long userId, String faceImageBase64) {
        if (Boolean.TRUE.equals(properties.getMockModeEnabled())) {
            log.info("【Mock人脸识别】录入 | 用户ID={}", userId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("faceId", "MOCK_FACE_" + userId);
            result.put("message", "Mock模式：人脸特征录入成功");
            return result;
        }
        log.warn("【真实人脸识别】暂未接入 | 用户ID={}", userId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "真实人脸识别暂未接入");
        return result;
    }

    private Map<String, Object> verifyFaceMock(Long userId, Long heirId) {
        log.info("【Mock人脸识别】核验 | 用户ID={} | 继承人ID={}", userId, heirId);
        Map<String, Object> result = new HashMap<>();
        result.put("verified", true);
        result.put("similarity", 98.5);
        result.put("confidence", 0.99);
        result.put("message", "Mock模式：人脸核验自动通过");
        return result;
    }

    private Map<String, Object> verifyFaceReal(Long userId, Long heirId) {
        log.warn("【真实人脸识别】暂未接入 | 用户ID={} | 继承人ID={}", userId, heirId);
        Map<String, Object> result = new HashMap<>();
        result.put("verified", false);
        result.put("message", "真实人脸识别暂未接入");
        return result;
    }
}
