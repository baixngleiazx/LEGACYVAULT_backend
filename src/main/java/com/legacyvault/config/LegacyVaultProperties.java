package com.legacyvault.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * LegacyVault 自定义配置属性
 * 对应 application.yml 中 legacy-vault 前缀的配置项
 *
 * @author LegacyVault
 */
@Data
@Component
@ConfigurationProperties(prefix = "legacy-vault")
public class LegacyVaultProperties {

    /** 全局Mock模式开关 */
    private Boolean mockModeEnabled = true;

    /** JWT配置 */
    private JwtConfig jwt = new JwtConfig();

    /** 心跳配置 */
    private HeartbeatConfig heartbeat = new HeartbeatConfig();

    /** 触发验证配置 */
    private TriggerConfig trigger = new TriggerConfig();

    /** 交付链接配置 */
    private DeliveryConfig delivery = new DeliveryConfig();

    /** TOTP配置 */
    private TotpConfig totp = new TotpConfig();

    /** 加密配置 */
    private CryptoConfig crypto = new CryptoConfig();

    /** 文件存储配置 */
    private StorageConfig storage = new StorageConfig();

    @Data
    public static class JwtConfig {
        /** JWT签名密钥 */
        private String secret = "LegacyVault_JWT_Secret_Key_2026_MOCK_ONLY";
        /** JWT有效期（小时） */
        private Integer expireHours = 24;
    }

    @Data
    public static class HeartbeatConfig {
        /** 默认打卡周期（天） */
        private Integer defaultPeriodDays = 90;
        /** 宽限期（小时） */
        private Integer gracePeriodHours = 72;
    }

    @Data
    public static class TriggerConfig {
        /** 联系人核查窗口（小时） */
        private Integer contactCheckHours = 24;
        /** 公证人通知窗口（小时） */
        private Integer notaryNotifyHours = 24;
        /** 最少确认失联联系人数 */
        private Integer minConfirmCount = 2;
    }

    @Data
    public static class DeliveryConfig {
        /** 交付链接有效期（天） */
        private Integer linkExpireDays = 7;
        /** 最大核验失败次数 */
        private Integer maxFailCount = 3;
    }

    @Data
    public static class TotpConfig {
        /** TOTP发行方名称 */
        private String issuer = "LegacyVault";
    }

    @Data
    public static class CryptoConfig {
        /** PBKDF2迭代次数 */
        private Integer pbkdf2Iterations = 310000;
    }

    @Data
    public static class StorageConfig {
        /** 本地存储路径（Mock阶段） */
        private String localPath = "./uploads/encrypted";
    }
}
