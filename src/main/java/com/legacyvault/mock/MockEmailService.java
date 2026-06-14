package com.legacyvault.mock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Mock邮件服务
 * 模拟邮件发送，记录发送日志，不实际发送邮件
 *
 * 【Mock模式】当前为模拟实现，所有邮件仅记录日志
 * 【切换正式】替换为Spring Mail / SendGrid / SES 等邮件SDK
 *
 * @author LegacyVault
 */
@Slf4j
@Service
public class MockEmailService {

    /**
     * 发送验证码邮件
     *
     * @param email      收件邮箱
     * @param verifyCode 验证码
     * @param purpose    用途说明（注册/登录等）
     * @return 是否发送成功
     */
    public boolean sendVerifyCode(String email, String verifyCode, String purpose) {
        log.info("【Mock邮件】发送验证码 | 邮箱={} | 验证码={} | 用途={}", email, verifyCode, purpose);
        log.info("【Mock邮件】提示：正式环境请使用Spring Mail或第三方邮件服务");
        return true;

        /*
         * ========== 正式接口预留（Spring Mail） ==========
         * 切换步骤：
         * 1. 引入依赖：spring-boot-starter-mail
         * 2. 配置SMTP（application.yml）：
         *    spring.mail.host=smtp.gmail.com
         *    spring.mail.port=587
         *    spring.mail.username=xxx
         *    spring.mail.password=xxx
         * 3. 使用 JavaMailSender 发送
         *
         * MimeMessage message = mailSender.createMimeMessage();
         * MimeMessageHelper helper = new MimeMessageHelper(message, true);
         * helper.setTo(email);
         * helper.setSubject("LegacyVault 验证码");
         * helper.setText("您的验证码是：" + verifyCode + "，5分钟内有效。");
         * mailSender.send(message);
         */
    }

    /**
     * 发送继承人确认邀请邮件
     *
     * @param email    继承人邮箱
     * @param userName 邀请人姓名
     * @param confirmUrl 确认链接
     * @return 是否发送成功
     */
    public boolean sendHeirConfirmInvite(String email, String userName, String confirmUrl) {
        log.info("【Mock邮件】继承人邀请 | 邮箱={} | 邀请人={} | 链接={}", email, userName, confirmUrl);
        return true;
    }

    /**
     * 发送心跳提醒邮件
     */
    public boolean sendHeartbeatRemind(String email, String userName, String deadline) {
        log.info("【Mock邮件】心跳提醒 | 邮箱={} | 用户={} | 截止={}", email, userName, deadline);
        return true;
    }

    /**
     * 发送触发告警邮件
     */
    public boolean sendTriggerAlert(String email, String userName) {
        log.info("【Mock邮件】触发告警 | 邮箱={} | 用户={}", email, userName);
        return true;
    }

    /**
     * 发送交付链接邮件
     */
    public boolean sendDeliveryLink(String email, String deliveryUrl, String userName) {
        log.info("【Mock邮件】交付链接 | 邮箱={} | 链接={} | 原用户={}", email, deliveryUrl, userName);
        return true;
    }

    /**
     * 发送可信联系人核查邮件
     */
    public boolean sendContactVerification(String email, String userName, String verifyUrl) {
        log.info("【Mock邮件】联系人核查 | 邮箱={} | 用户={} | 链接={}", email, userName, verifyUrl);
        return true;
    }
}
