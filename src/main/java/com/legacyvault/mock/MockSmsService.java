package com.legacyvault.mock;

import com.legacyvault.config.LegacyVaultProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 短信服务（Mock / 真实双分支）
 *
 * @author LegacyVault
 */
@Slf4j
@Service
public class MockSmsService {

    @Autowired
    private LegacyVaultProperties properties;

    public boolean sendVerifyCode(String phone, String verifyCode) {
        if (Boolean.TRUE.equals(properties.getMockModeEnabled())) {
            log.info("【Mock短信】发送验证码 | 手机号={} | 验证码={}", phone, verifyCode);
            return true;
        }
        log.warn("【真实短信】暂未接入，请配置阿里云 SMS / 腾讯云 SMS | 手机号={}", phone);
        return false;
    }

    public boolean sendHeartbeatRemind(String phone, String userName, String deadline) {
        if (Boolean.TRUE.equals(properties.getMockModeEnabled())) {
            log.info("【Mock短信】心跳提醒 | 手机号={} | 用户={} | 截止={}", phone, userName, deadline);
            return true;
        }
        log.warn("【真实短信】心跳提醒暂未接入 | 手机号={}", phone);
        return false;
    }

    public boolean sendTriggerAlert(String phone, String userName) {
        if (Boolean.TRUE.equals(properties.getMockModeEnabled())) {
            log.info("【Mock短信】触发告警 | 手机号={} | 用户={}", phone, userName);
            return true;
        }
        log.warn("【真实短信】触发告警暂未接入 | 手机号={}", phone);
        return false;
    }
}
