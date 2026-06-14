package com.legacyvault.module.user.service.impl;

import com.legacyvault.common.Constants;
import com.legacyvault.common.ResultCode;
import com.legacyvault.exception.BusinessException;
import com.legacyvault.mock.MockKycService;
import com.legacyvault.module.auth.service.AuditLogService;
import com.legacyvault.module.user.dto.UserInfoResponse;
import com.legacyvault.module.user.entity.User;
import com.legacyvault.module.user.mapper.UserMapper;
import com.legacyvault.module.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

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
    private MockKycService mockKycService;

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
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (user.getKycStatus() == Constants.KYC_STATUS_PASSED) {
            throw new BusinessException("KYC已通过，无需重复提交");
        }

        // 调用Mock KYC服务
        Map<String, Object> result = mockKycService.submitKyc(userId, name, idCardNo);

        // 更新KYC状态
        String status = (String) result.get("status");
        if ("PASSED".equals(status)) {
            user.setKycStatus(Constants.KYC_STATUS_PASSED);
            user.setSecurityScore(Math.min(100, user.getSecurityScore() + 30));
        } else {
            user.setKycStatus(Constants.KYC_STATUS_REJECTED);
        }
        userMapper.updateById(user);

        auditLogService.log(userId, Constants.AUDIT_MODULE_AUTH, "submit_kyc",
                String.format("{\"result\":\"%s\"}", status));
    }
}
