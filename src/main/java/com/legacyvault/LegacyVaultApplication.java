package com.legacyvault;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * LegacyVault 数字遗产信托锁平台 - 后端启动类
 *
 * @author LegacyVault
 * @since 1.0.0
 */
@SpringBootApplication
@MapperScan("com.legacyvault.module.*.mapper")
@EnableScheduling  // 启用定时任务（心跳检测、提醒调度等）
public class LegacyVaultApplication {

    public static void main(String[] args) {
        SpringApplication.run(LegacyVaultApplication.class, args);
        System.out.println("========================================");
        System.out.println("  LegacyVault 后端服务启动成功");
        System.out.println("  端口: 8080");
        System.out.println("  Mock模式: 已启用（所有外部接口使用模拟数据）");
        System.out.println("========================================");
    }
}
