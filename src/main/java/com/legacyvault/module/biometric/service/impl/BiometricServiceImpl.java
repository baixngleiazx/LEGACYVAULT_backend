package com.legacyvault.module.biometric.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.legacyvault.common.Constants;
import com.legacyvault.common.ResultCode;
import com.legacyvault.exception.BusinessException;
import com.legacyvault.mock.MockFaceRecognitionService;
import com.legacyvault.module.auth.service.AuditLogService;
import com.legacyvault.module.biometric.dto.BiometricRegisterRequest;
import com.legacyvault.module.biometric.dto.BiometricStatusResponse;
import com.legacyvault.module.biometric.entity.BiometricRecord;
import com.legacyvault.module.biometric.mapper.BiometricRecordMapper;
import com.legacyvault.module.biometric.service.BiometricService;
import com.legacyvault.module.user.entity.User;
import com.legacyvault.module.user.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 生物特征服务实现
 *
 * @author LegacyVault
 */
@Slf4j
@Service
public class BiometricServiceImpl implements BiometricService {

    @Autowired
    private BiometricRecordMapper biometricRecordMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MockFaceRecognitionService mockFaceRecognitionService;

    @Autowired
    private AuditLogService auditLogService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(Long userId, BiometricRegisterRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        String biometricType = request.getBiometricType().toUpperCase();
        if (!Constants.BIOMETRIC_TYPE_FACE.equals(biometricType)
                && !Constants.BIOMETRIC_TYPE_FINGER.equals(biometricType)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "不支持的生物特征类型");
        }

        // 调用 Mock / 真实 生物识别服务（此处仅做日志记录与校验）
        mockFaceRecognitionService.registerFace(userId, request.getFeatureHash());

        // 写入记录（仅存哈希，不存原始图像）
        BiometricRecord record = new BiometricRecord();
        record.setUserId(userId);
        record.setBiometricType(biometricType);
        record.setFeatureHash(request.getFeatureHash());
        record.setDeviceInfo(request.getDeviceInfo());
        record.setBoundAt(LocalDateTime.now());
        biometricRecordMapper.insert(record);

        // 更新用户状态
        user.setBiometricBound(1);
        user.setSecurityScore(Math.min(100, (user.getSecurityScore() != null ? user.getSecurityScore() : 0) + 10));
        userMapper.updateById(user);

        auditLogService.log(userId, Constants.AUDIT_MODULE_BIOMETRIC, "register",
                String.format("{\"type\":\"%s\"}", biometricType));
        log.info("生物特征录入 | userId={} | type={}", userId, biometricType);
    }

    @Override
    public BiometricStatusResponse getStatus(Long userId) {
        BiometricRecord record = biometricRecordMapper.selectOne(
                new LambdaQueryWrapper<BiometricRecord>()
                        .eq(BiometricRecord::getUserId, userId)
                        .orderByDesc(BiometricRecord::getBoundAt)
                        .last("LIMIT 1"));

        BiometricStatusResponse resp = new BiometricStatusResponse();
        if (record == null) {
            resp.setBound(false);
            return resp;
        }
        resp.setBound(true);
        resp.setBiometricType(record.getBiometricType());
        resp.setDeviceInfo(record.getDeviceInfo());
        resp.setBoundAt(record.getBoundAt());
        return resp;
    }
}
