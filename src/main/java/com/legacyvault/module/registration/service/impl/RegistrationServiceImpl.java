package com.legacyvault.module.registration.service.impl;

import com.legacyvault.common.Constants;
import com.legacyvault.common.ResultCode;
import com.legacyvault.exception.BusinessException;
import com.legacyvault.module.auth.service.AuditLogService;
import com.legacyvault.module.registration.dto.RegistrationStatusResponse;
import com.legacyvault.module.registration.dto.StepStatusVo;
import com.legacyvault.module.registration.service.RegistrationService;
import com.legacyvault.module.user.entity.User;
import com.legacyvault.module.user.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 注册流程服务实现
 *
 * @author LegacyVault
 */
@Slf4j
@Service
public class RegistrationServiceImpl implements RegistrationService {

    /** 每步的风险提示文案（跳过时展示），索引 0~4 对应步骤 1~5 */
    private static final String[] SKIP_RISK_HINTS = {
            "开户后信息不可随意修改，请确认填写准确",
            "跳过 TOTP 绑定会显著降低账户安全性，强烈建议完成绑定",
            "跳过生物特征录入将无法使用高风险操作的二次生物确认",
            "跳过 KYC 核验后，若账户资产超过阈值将被强制要求补充 KYC",
            "跳过恢复码生成后，丢失 TOTP 设备将无法找回账户"
    };

    /** 步骤名，索引 0~4 对应步骤 1~5 */
    private static final String[] STEP_NAMES = {
            "开户", "TOTP 绑定", "生物特征录入", "KYC 身份核验", "离线恢复码"
    };

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AuditLogService auditLogService;

    @Override
    public RegistrationStatusResponse getStatus(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        RegistrationStatusResponse resp = new RegistrationStatusResponse();
        resp.setPlanId(user.getPlanId());
        resp.setPlanName(resolvePlanName(user.getPlanId()));
        resp.setTotpBound(user.getTotpBound() != null && user.getTotpBound() == Constants.TOTP_BOUND);

        List<StepStatusVo> steps = new ArrayList<>();
        Integer[] doneArr = {user.getStep1Done(), user.getStep2Done(), user.getStep3Done(),
                user.getStep4Done(), user.getStep5Done()};
        Integer[] skippedArr = {user.getStep1Skipped(), user.getStep2Skipped(), user.getStep3Skipped(),
                user.getStep4Skipped(), user.getStep5Skipped()};

        int firstIncomplete = -1;
        boolean allCompleted = true;
        for (int i = 0; i < 5; i++) {
            StepStatusVo vo = new StepStatusVo();
            vo.setStep(i + 1);
            vo.setStepName(STEP_NAMES[i]);
            Integer done = doneArr[i] != null ? doneArr[i] : 0;
            Integer skipped = skippedArr[i] != null ? skippedArr[i] : 0;
            vo.setDone(done);
            vo.setSkipped(skipped);
            vo.setSkipRiskHint(SKIP_RISK_HINTS[i]);
            boolean stepCompleted = done == Constants.STEP_DONE || skipped == Constants.STEP_SKIPPED;
            vo.setAccessible(stepCompleted || firstIncomplete == -1);
            if (!stepCompleted) {
                allCompleted = false;
                if (firstIncomplete == -1) {
                    firstIncomplete = i + 1;
                }
            }
            steps.add(vo);
        }
        resp.setSteps(steps);
        resp.setAllCompleted(allCompleted);
        resp.setCurrentStep(allCompleted ? null : firstIncomplete);
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeStep(Long userId, int step) {
        validateStep(step);
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        setStepDone(user, step, Constants.STEP_DONE);
        setStepSkipped(user, step, Constants.STEP_NOT_SKIPPED);
        userMapper.updateById(user);

        auditLogService.log(userId, Constants.AUDIT_MODULE_REGISTRATION, "complete_step",
                String.format("{\"step\":%d}", step));
        log.info("注册步骤 {} 完成 | userId={}", step, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void skipStep(Long userId, int step) {
        validateStep(step);
        // 步骤 1 不允许跳过（已开户的用户此接口无意义，但强制保护）
        if (step == Constants.REG_STEP_ACCOUNT) {
            throw new BusinessException("步骤 1（开户）不允许跳过");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        setStepDone(user, step, Constants.STEP_NOT_DONE);
        setStepSkipped(user, step, Constants.STEP_SKIPPED);
        userMapper.updateById(user);

        auditLogService.log(userId, Constants.AUDIT_MODULE_REGISTRATION, "skip_step",
                String.format("{\"step\":%d}", step));
        log.info("注册步骤 {} 跳过 | userId={}", step, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeAll(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        // 标记全部步骤完成（未完成的置为完成）
        for (int i = 1; i <= 5; i++) {
            Integer done = getStepDone(user, i);
            Integer skipped = getStepSkipped(user, i);
            if ((done == null || done == 0) && (skipped == null || skipped == 0)) {
                setStepDone(user, i, Constants.STEP_DONE);
            }
        }
        userMapper.updateById(user);

        auditLogService.log(userId, Constants.AUDIT_MODULE_REGISTRATION, "complete_all", null);
        log.info("5 步注册流程全部完成 | userId={}", userId);
    }

    private void validateStep(int step) {
        if (step < 1 || step > 5) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "步骤编号必须为 1-5");
        }
    }

    private Integer getStepDone(User user, int step) {
        switch (step) {
            case 1: return user.getStep1Done();
            case 2: return user.getStep2Done();
            case 3: return user.getStep3Done();
            case 4: return user.getStep4Done();
            case 5: return user.getStep5Done();
            default: return 0;
        }
    }

    private Integer getStepSkipped(User user, int step) {
        switch (step) {
            case 1: return user.getStep1Skipped();
            case 2: return user.getStep2Skipped();
            case 3: return user.getStep3Skipped();
            case 4: return user.getStep4Skipped();
            case 5: return user.getStep5Skipped();
            default: return 0;
        }
    }

    private void setStepDone(User user, int step, int value) {
        switch (step) {
            case 1: user.setStep1Done(value); break;
            case 2: user.setStep2Done(value); break;
            case 3: user.setStep3Done(value); break;
            case 4: user.setStep4Done(value); break;
            case 5: user.setStep5Done(value); break;
        }
    }

    private void setStepSkipped(User user, int step, int value) {
        switch (step) {
            case 1: user.setStep1Skipped(value); break;
            case 2: user.setStep2Skipped(value); break;
            case 3: user.setStep3Skipped(value); break;
            case 4: user.setStep4Skipped(value); break;
            case 5: user.setStep5Skipped(value); break;
        }
    }

    private String resolvePlanName(Long planId) {
        if (planId == null) return "Free";
        if (planId == Constants.PLAN_PRO) return "Pro";
        if (planId == Constants.PLAN_VAULT) return "Vault";
        return "Free";
    }
}
