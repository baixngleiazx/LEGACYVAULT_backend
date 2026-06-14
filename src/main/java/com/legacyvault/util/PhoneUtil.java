package com.legacyvault.util;

import java.util.regex.Pattern;

/**
 * 手机号校验工具类
 * 支持中国大陆手机号格式校验
 *
 * @author LegacyVault
 */
public class PhoneUtil {

    private PhoneUtil() {}

    /**
     * 中国大陆手机号正则：1开头，第二位3-9，共11位数字
     */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    /**
     * 邮箱正则（简化版）
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$");

    /**
     * 校验手机号格式
     *
     * @param phone 手机号
     * @return 是否为合法手机号
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * 校验邮箱格式
     *
     * @param email 邮箱
     * @return 是否为合法邮箱
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * 自动识别目标类型：phone 或 email
     *
     * @param target 手机号或邮箱
     * @return "phone" / "email" / null（无法识别）
     */
    public static String detectTargetType(String target) {
        if (target == null || target.isEmpty()) {
            return null;
        }
        if (isValidPhone(target)) {
            return "phone";
        }
        if (isValidEmail(target)) {
            return "email";
        }
        return null;
    }

    /**
     * 根据目标类型返回发送渠道
     *
     * @param targetType "phone" 或 "email"
     * @return "sms" 或 "email"
     */
    public static String getChannelByTargetType(String targetType) {
        return "phone".equals(targetType) ? "sms" : "email";
    }
}
