package com.legacyvault.util;

import cn.hutool.core.codec.Base32;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

/**
 * TOTP 工具类
 * 实现基于时间的一次性密码（RFC 6238）
 * 支持 Google Authenticator / YubiKey 等
 *
 * @author LegacyVault
 */
@Slf4j
public class TotpUtil {

    /** 时间步长（30秒） */
    private static final int TIME_STEP_SECONDS = 30;
    /** 密码长度（6位） */
    private static final int CODE_DIGITS = 6;
    /** 哈希算法 */
    private static final String ALGORITHM = "HmacSHA1";

    /**
     * 生成TOTP密钥（Base32编码）
     *
     * @return Base32编码的密钥
     */
    public static String generateSecret() {
        byte[] buffer = new byte[20];
        new SecureRandom().nextBytes(buffer);
        return Base32.encode(buffer);
    }

    /**
     * 根据密钥生成当前时间的TOTP码
     *
     * @param secret Base32编码的密钥
     * @return 6位数字验证码
     */
    public static String generateCode(String secret) {
        long timeIndex = System.currentTimeMillis() / 1000 / TIME_STEP_SECONDS;
        return generateCodeAtTime(secret, timeIndex);
    }

    /**
     * 验证TOTP码是否正确（允许前后各1个时间步长的偏差）
     *
     * @param secret Base32编码的密钥
     * @param code   用户输入的验证码
     * @return 是否验证通过
     */
    public static boolean verifyCode(String secret, String code) {
        if (code == null || code.length() != CODE_DIGITS) {
            return false;
        }
        long timeIndex = System.currentTimeMillis() / 1000 / TIME_STEP_SECONDS;
        // 允许前后各1个时间步长的偏差（共3个窗口）
        for (int i = -1; i <= 1; i++) {
            String expected = generateCodeAtTime(secret, timeIndex + i);
            if (expected.equals(code)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 生成指定时间步长的TOTP码
     */
    private static String generateCodeAtTime(String secret, long timeIndex) {
        try {
            byte[] keyBytes = Base32.decode(secret);
            byte[] timeBytes = new byte[8];
            for (int i = 7; i >= 0; i--) {
                timeBytes[i] = (byte) (timeIndex & 0xff);
                timeIndex >>= 8;
            }

            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(keyBytes, ALGORITHM));
            byte[] hash = mac.doFinal(timeBytes);

            int offset = hash[hash.length - 1] & 0x0f;
            int binary = ((hash[offset] & 0x7f) << 24)
                    | ((hash[offset + 1] & 0xff) << 16)
                    | ((hash[offset + 2] & 0xff) << 8)
                    | (hash[offset + 3] & 0xff);

            int otp = binary % (int) Math.pow(10, CODE_DIGITS);
            return String.format("%06d", otp);
        } catch (Exception e) {
            log.error("TOTP码生成失败: {}", e.getMessage());
            return "000000";
        }
    }

    /**
     * 生成TOTP绑定URI（用于生成二维码）
     *
     * @param issuer  发行方名称
     * @param account 用户账户（通常是邮箱）
     * @param secret  Base32密钥
     * @return otpauth URI
     */
    public static String getOtpAuthUri(String issuer, String account, String secret) {
        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=%d&period=%d",
                issuer, account, secret, issuer, CODE_DIGITS, TIME_STEP_SECONDS);
    }
}
