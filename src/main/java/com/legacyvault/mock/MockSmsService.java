package com.legacyvault.mock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Mock短信服务
 * 模拟短信发送，记录发送日志，不实际发送短信
 *
 * 【Mock模式】当前为模拟实现，所有短信仅记录日志
 * 【切换正式】替换为阿里云短信/腾讯云短信SDK调用
 *
 * @author LegacyVault
 */
@Slf4j
@Service
public class MockSmsService {

    /**
     * 发送短信验证码
     *
     * @param phone      手机号
     * @param verifyCode 验证码
     * @return 是否发送成功（Mock始终返回true）
     */
    public boolean sendVerifyCode(String phone, String verifyCode) {
        // ========== Mock实现 ==========
        // 模拟短信发送，仅记录日志
        log.info("【Mock短信】发送验证码 | 手机号={} | 验证码={}", phone, verifyCode);
        log.info("【Mock短信】提示：正式环境请替换为阿里云短信SDK调用");

        /*
         * ========== 正式接口预留（阿里云短信） ==========
         * 切换步骤：
         * 1. 引入依赖：aliyun-java-sdk-dysmsapi
         * 2. 配置AccessKeyId/Secret（application.yml）
         * 3. 调用 CommonRequest 发送短信
         * 4. 解析返回结果判断是否成功
         *
         * CommonRequest request = new CommonRequest();
         * request.setSysDomain("dysmsapi.aliyuncs.com");
         * request.setSysAction("SendSms");
         * request.putQueryParameter("PhoneNumbers", phone);
         * request.putQueryParameter("SignName", "LegacyVault");
         * request.putQueryParameter("TemplateCode", "SMS_XXXXXX");
         * request.putQueryParameter("TemplateParam", "{\"code\":\"" + verifyCode + "\"}");
         */
        return true;
    }

    /**
     * 发送心跳提醒短信
     *
     * @param phone    手机号
     * @param userName 用户昵称
     * @param deadline 打卡截止日期
     * @return 是否发送成功
     */
    public boolean sendHeartbeatRemind(String phone, String userName, String deadline) {
        log.info("【Mock短信】心跳提醒 | 手机号={} | 用户={} | 截止={}", phone, userName, deadline);
        return true;
    }

    /**
     * 发送触发告警短信
     */
    public boolean sendTriggerAlert(String phone, String userName) {
        log.info("【Mock短信】触发告警 | 手机号={} | 用户={}", phone, userName);
        return true;
    }
}
