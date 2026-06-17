package com.legacyvault.mock;

import com.legacyvault.config.LegacyVaultProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 邮件服务（Mock / 真实双分支）
 *
 * Mock 模式：仅记录日志
 * 真实模式：调用 Spring Mail / SendGrid / SES
 *
 * @author LegacyVault
 */
@Slf4j
@Service
public class MockEmailService {

    @Autowired
    private LegacyVaultProperties properties;

    public boolean sendVerifyCode(String email, String verifyCode, String purpose) {
        if (Boolean.TRUE.equals(properties.getMockModeEnabled())) {
            log.info("【Mock邮件】发送验证码 | 邮箱={} | 验证码={} | 用途={}", email, verifyCode, purpose);
            return true;
        }
        log.warn("【真实邮件】暂未接入，请配置 Spring Mail / SendGrid | 邮箱={}", email);
        return false;
    }

    public boolean sendHeirConfirmInvite(String email, String userName, String confirmUrl) {
        if (Boolean.TRUE.equals(properties.getMockModeEnabled())) {
            log.info("【Mock邮件】继承人邀请 | 邮箱={} | 邀请人={} | 链接={}", email, userName, confirmUrl);
            return true;
        }
        log.warn("【真实邮件】继承人邀请暂未接入 | 邮箱={}", email);
        return false;
    }

    public boolean sendHeartbeatRemind(String email, String userName, String deadline) {
        if (Boolean.TRUE.equals(properties.getMockModeEnabled())) {
            log.info("【Mock邮件】心跳提醒 | 邮箱={} | 用户={} | 截止={}", email, userName, deadline);
            return true;
        }
        log.warn("【真实邮件】心跳提醒暂未接入 | 邮箱={}", email);
        return false;
    }

    public boolean sendTriggerAlert(String email, String userName) {
        if (Boolean.TRUE.equals(properties.getMockModeEnabled())) {
            log.info("【Mock邮件】触发告警 | 邮箱={} | 用户={}", email, userName);
            return true;
        }
        log.warn("【真实邮件】触发告警暂未接入 | 邮箱={}", email);
        return false;
    }

    public boolean sendDeliveryLink(String email, String deliveryUrl, String userName) {
        if (Boolean.TRUE.equals(properties.getMockModeEnabled())) {
            log.info("【Mock邮件】交付链接 | 邮箱={} | 链接={} | 原用户={}", email, deliveryUrl, userName);
            return true;
        }
        log.warn("【真实邮件】交付链接暂未接入 | 邮箱={}", email);
        return false;
    }

    public boolean sendContactVerification(String email, String userName, String verifyUrl) {
        if (Boolean.TRUE.equals(properties.getMockModeEnabled())) {
            log.info("【Mock邮件】联系人核查 | 邮箱={} | 用户={} | 链接={}", email, userName, verifyUrl);
            return true;
        }
        log.warn("【真实邮件】联系人核查暂未接入 | 邮箱={}", email);
        return false;
    }
}
