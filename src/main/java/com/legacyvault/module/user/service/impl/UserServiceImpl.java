package com.legacyvault.module.user.service.impl;

import com.legacyvault.common.Constants;
import com.legacyvault.common.ResultCode;
import com.legacyvault.exception.BusinessException;
import com.legacyvault.module.auth.service.AuditLogService;
import com.legacyvault.module.kyc.dto.KycSubmitRequest;
import com.legacyvault.module.kyc.service.KycService;
import com.legacyvault.module.user.dto.UserInfoResponse;
import com.legacyvault.module.user.entity.User;
import com.legacyvault.module.user.mapper.UserMapper;
import com.legacyvault.module.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现
 *
 * @author LegacyVault
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private KycService kycService;

    @Autowired
    private AuditLogService auditLogService;

    @Override
    public UserInfoResponse getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        UserInfoResponse response = new UserInfoResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setNickname(user.getNickname());
        response.setStatus(user.getStatus());
        response.setTotpBound(user.getTotpBound());
        response.setBiometricBound(user.getBiometricBound());
        response.setKycStatus(user.getKycStatus());
        response.setSecurityScore(user.getSecurityScore());
        response.setTravelModeEnabled(user.getTravelModeEnabled());
        response.setTravelStartDate(user.getTravelStartDate());
        response.setTravelEndDate(user.getTravelEndDate());
        response.setPlanId(user.getPlanId());
        // 5 步注册流程状态
        response.setStep1Done(user.getStep1Done());
        response.setStep2Done(user.getStep2Done());
        response.setStep3Done(user.getStep3Done());
        response.setStep4Done(user.getStep4Done());
        response.setStep5Done(user.getStep5Done());
        response.setStep1Skipped(user.getStep1Skipped());
        response.setStep2Skipped(user.getStep2Skipped());
        response.setStep3Skipped(user.getStep3Skipped());
        response.setStep4Skipped(user.getStep4Skipped());
        response.setStep5Skipped(user.getStep5Skipped());
        // 继承人与 KYC 门槛
        response.setPlanHeirLimit(user.getPlanHeirLimit());
        response.setMinHeirsToUnlock(user.getMinHeirsToUnlock());
        response.setKycAssetThreshold(user.getKycAssetThreshold());
        response.setKycRejectReason(user.getKycRejectReason());
        response.setLastLoginAt(user.getLastLoginAt());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }

    @Override
    public void updateNickname(Long userId, String nickname) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        user.setNickname(nickname);
        userMapper.updateById(user);
        auditLogService.log(userId, "user", "update_nickname",
                String.format("{\"nickname\":\"%s\"}", nickname));
    }

    @Override
    public void submitKyc(Long userId, String name, String idCardNo) {
        // 委托给 KycService（保持原接口兼容性）
        KycSubmitRequest request = new KycSubmitRequest();
        request.setRealName(name);
        request.setIdNo(idCardNo);
        kycService.submit(userId, request);
    }
}
