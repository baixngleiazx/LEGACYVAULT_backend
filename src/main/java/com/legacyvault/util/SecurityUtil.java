package com.legacyvault.util;

import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.UUID;

/**
 * 安全工具类
 * 提供密码哈希、SHA-256、随机码生成等安全相关功能
 *
 * @author LegacyVault
 */
public class SecurityUtil {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * 生成SHA-256哈希
     *
     * @param input 输入字符串
     * @return SHA-256哈希值（十六进制）
     */
    public static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256计算失败", e);
        }
    }

    /**
     * 使用Spring的MD5工具生成哈希（简化版，用于验证码等）
     */
    public static String md5(String input) {
        return DigestUtils.md5DigestAsHex(input.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成随机验证码（纯数字）
     *
     * @param length 验证码长度
     * @return 随机验证码
     */
    public static String generateVerifyCode(int length) {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(SECURE_RANDOM.nextInt(10));
        }
        return code.toString();
    }

    /**
     * 生成16位紧急恢复码
     *
     * @return 16位随机恢复码
     */
    public static String generateRecoveryCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    /**
     * 生成唯一Token（URL安全）
     *
     * @return 32位随机Token
     */
    public static String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * BCrypt密码哈希（使用Spring Security的BCrypt）
     * 简化实现：使用SHA-256 + Salt模拟
     * 正式环境建议引入spring-security-crypto使用真正的BCrypt
     */
    public static String hashPassword(String password) {
        String salt = generateToken().substring(0, 16);
        return salt + ":" + sha256(salt + password);
    }

    /**
     * 验证密码
     */
    public static boolean verifyPassword(String password, String hashedPassword) {
        if (hashedPassword == null || !hashedPassword.contains(":")) {
            return false;
        }
        String[] parts = hashedPassword.split(":", 2);
        String salt = parts[0];
        String expectedHash = parts[1];
        String actualHash = sha256(salt + password);
        return expectedHash.equals(actualHash);
    }
}
