package com.legacyvault.mock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Mock人脸识别服务
 * 模拟人脸核验（用于继承人身份验证），默认返回核验通过
 *
 * 【Mock模式】当前为模拟实现，直接返回核验通过
 * 【切换正式】替换为阿里云实人认证/腾讯云人脸核身SDK
 *
 * @author LegacyVault
 */
@Slf4j
@Service
public class MockFaceRecognitionService {

    /**
     * 人脸比对核验
     * 将当前人脸与注册时证件照比对
     *
     * @param userId     用户ID
     * @param heirId     继承人ID
     * @param faceImageBase64 当前人脸图片Base64（Mock不实际使用）
     * @return 核验结果
     */
    public Map<String, Object> verifyFace(Long userId, Long heirId, String faceImageBase64) {
        log.info("【Mock人脸识别】核验 | 用户ID={} | 继承人ID={}", userId, heirId);

        // Mock：直接返回核验通过，相似度98%
        Map<String, Object> result = new HashMap<>();
        result.put("verified", true);
        result.put("similarity", 98.5);
        result.put("confidence", 0.99);
        result.put("message", "Mock模式：人脸核验自动通过");

        log.info("【Mock人脸识别】核验通过 | 相似度=98.5%");

        /*
         * ========== 正式接口预留（阿里云实人认证） ==========
         * 切换步骤：
         * 1. 引入依赖：aliyun-java-sdk-cloudauth
         * 2. 配置AccessKeyId/Secret
         * 3. 调用 InitFaceVerify → 获取verifyToken → 前端H5拉起人脸采集
         * 4. 调用 DescribeFaceVerify 获取核验结果
         *
         * InitFaceVerifyRequest request = new InitFaceVerifyRequest();
         * request.setProductCode("ID_PRO");
         * request.setModel("LIVENESS");
         * request.setMetaInfo(metaInfo);
         * InitFaceVerifyResponse response = client.initFaceVerify(request);
         */
        return result;
    }

    /**
     * 录入人脸特征（注册阶段）
     *
     * @param userId 用户ID
     * @param faceImageBase64 人脸图片Base64
     * @return 录入结果
     */
    public Map<String, Object> registerFace(Long userId, String faceImageBase64) {
        log.info("【Mock人脸识别】录入 | 用户ID={}", userId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("faceId", "MOCK_FACE_" + userId);
        result.put("message", "Mock模式：人脸特征录入成功");
        return result;
    }
}
