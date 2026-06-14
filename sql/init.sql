-- ============================================================
-- LegacyVault 数字遗产平台 - 数据库初始化脚本
-- 数据库：MySQL 8.0
-- 字符集：utf8mb4
-- 命名规范：下划线命名（snake_case）
-- ============================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS legacy_vault DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE legacy_vault;

-- ============================================================
-- 一、用户与认证模块
-- ============================================================

-- 1.1 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `email` VARCHAR(128) NOT NULL COMMENT '邮箱',
    `phone` VARCHAR(32) DEFAULT NULL COMMENT '手机号',
    `password_hash` VARCHAR(256) NOT NULL COMMENT '密码哈希（BCrypt）',
    `nickname` VARCHAR(64) DEFAULT NULL COMMENT '昵称',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-正常 2-锁定',
    `plan_id` BIGINT DEFAULT NULL COMMENT '当前套餐ID',
    `plan_expires_at` DATETIME DEFAULT NULL COMMENT '套餐到期时间',
    `totp_bound` TINYINT NOT NULL DEFAULT 0 COMMENT 'TOTP是否已绑定：0-否 1-是',
    `biometric_bound` TINYINT NOT NULL DEFAULT 0 COMMENT '生物特征是否已录入：0-否 1-是',
    `kyc_status` TINYINT NOT NULL DEFAULT 0 COMMENT 'KYC状态：0-未认证 1-已提交 2-已通过 3-已拒绝',
    `security_score` INT NOT NULL DEFAULT 0 COMMENT '安全健康分（0-100）',
    `travel_mode_enabled` TINYINT NOT NULL DEFAULT 0 COMMENT '旅行模式是否开启：0-否 1-是',
    `travel_start_date` DATE DEFAULT NULL COMMENT '旅行模式开始日期',
    `travel_end_date` DATE DEFAULT NULL COMMENT '旅行模式结束日期',
    `last_login_at` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_email` (`email`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 1.2 TOTP配置表
DROP TABLE IF EXISTS `totp_config`;
CREATE TABLE `totp_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `secret_key` VARCHAR(128) NOT NULL COMMENT 'TOTP密钥（加密存储）',
    `issuer` VARCHAR(64) NOT NULL DEFAULT 'LegacyVault' COMMENT '发行方名称',
    `device_type` VARCHAR(32) DEFAULT NULL COMMENT '设备类型：app-手机应用/hardware-硬件密钥',
    `bound_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='TOTP配置表';

-- 1.3 紧急恢复码表
DROP TABLE IF EXISTS `recovery_code`;
CREATE TABLE `recovery_code` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `code_hash` VARCHAR(256) NOT NULL COMMENT '恢复码哈希（仅存哈希，不存明文）',
    `is_used` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已使用：0-未使用 1-已使用',
    `used_at` DATETIME DEFAULT NULL COMMENT '使用时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_code_hash` (`code_hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='紧急恢复码表';

-- ============================================================
-- 二、继承人与可信联系人模块
-- ============================================================

-- 2.1 继承人表
DROP TABLE IF EXISTS `heir`;
CREATE TABLE `heir` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '所属用户ID',
    `name` VARCHAR(64) NOT NULL COMMENT '继承人姓名',
    `email` VARCHAR(128) NOT NULL COMMENT '继承人邮箱',
    `phone` VARCHAR(32) DEFAULT NULL COMMENT '继承人手机号',
    `id_card_no` VARCHAR(64) DEFAULT NULL COMMENT '证件号（加密存储）',
    `confirmation_status` TINYINT NOT NULL DEFAULT 0 COMMENT '确认状态：0-待确认 1-已确认 2-已拒绝',
    `confirmation_token` VARCHAR(128) DEFAULT NULL COMMENT '确认邀请Token',
    `confirmed_at` DATETIME DEFAULT NULL COMMENT '确认时间',
    `assigned_content_count` INT NOT NULL DEFAULT 0 COMMENT '分配的内容数量',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_confirmation_status` (`confirmation_status`),
    KEY `idx_confirmation_token` (`confirmation_token`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='继承人表';

-- 2.2 继承人资产分配表
DROP TABLE IF EXISTS `heir_content_assign`;
CREATE TABLE `heir_content_assign` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `heir_id` BIGINT NOT NULL COMMENT '继承人ID',
    `content_id` BIGINT NOT NULL COMMENT '加密内容ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_heir_content` (`heir_id`, `content_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='继承人资产分配表';

-- 2.3 可信联系人表
DROP TABLE IF EXISTS `trusted_contact`;
CREATE TABLE `trusted_contact` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `name` VARCHAR(64) NOT NULL COMMENT '联系人姓名',
    `email` VARCHAR(128) NOT NULL COMMENT '联系人邮箱',
    `phone` VARCHAR(32) DEFAULT NULL COMMENT '联系人手机号',
    `relationship` VARCHAR(32) DEFAULT NULL COMMENT '与用户关系：family/friend/lawyer/other',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='可信联系人表';

-- ============================================================
-- 三、加密内容托管模块
-- ============================================================

-- 3.1 加密内容表
DROP TABLE IF EXISTS `encrypted_content`;
CREATE TABLE `encrypted_content` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '所属用户ID',
    `content_type` VARCHAR(32) NOT NULL COMMENT '内容类型：private_key/account_password/last_words/file',
    `title` VARCHAR(128) NOT NULL COMMENT '内容标题（用户自定义标识）',
    `encrypted_data` LONGTEXT NOT NULL COMMENT '加密后的数据（Base64编码）',
    `content_hash` VARCHAR(128) NOT NULL COMMENT '原始内容SHA-256哈希（用于完整性验证）',
    `file_name` VARCHAR(256) DEFAULT NULL COMMENT '原始文件名（仅file类型）',
    `file_size` BIGINT DEFAULT NULL COMMENT '文件大小（字节）',
    `storage_path` VARCHAR(512) DEFAULT NULL COMMENT '密文存储路径（本地/Arweave CID，Mock阶段存本地）',
    `k2_shard` VARCHAR(512) DEFAULT NULL COMMENT 'K2分片（HSM托管，Mock存库）',
    `k3_shard` VARCHAR(512) DEFAULT NULL COMMENT 'K3分片（第三方节点托管，Mock存库）',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-已删除 1-正常 2-已交付',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_content_type` (`content_type`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='加密内容表';

-- 3.2 密钥分片记录表
DROP TABLE IF EXISTS `key_shard`;
CREATE TABLE `key_shard` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `content_id` BIGINT NOT NULL COMMENT '关联加密内容ID',
    `shard_index` TINYINT NOT NULL COMMENT '分片序号：1-K1本地 2-K2 HSM 3-K3第三方',
    `shard_data` VARCHAR(1024) NOT NULL COMMENT '分片数据（加密存储）',
    `storage_location` VARCHAR(32) NOT NULL COMMENT '存储位置：local/hsm/third_party',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_content` (`user_id`, `content_id`),
    KEY `idx_shard_index` (`shard_index`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='密钥分片记录表';

-- ============================================================
-- 四、心跳打卡系统模块
-- ============================================================

-- 4.1 心跳配置表
DROP TABLE IF EXISTS `heartbeat_config`;
CREATE TABLE `heartbeat_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `check_in_period_days` INT NOT NULL DEFAULT 90 COMMENT '打卡周期（天）：30/60/90/180',
    `next_deadline` DATETIME NOT NULL COMMENT '下次打卡截止日期',
    `last_check_in_at` DATETIME DEFAULT NULL COMMENT '上次打卡时间',
    `remind_14d_sent` TINYINT NOT NULL DEFAULT 0 COMMENT 'T-14天提醒是否已发送',
    `remind_7d_sent` TINYINT NOT NULL DEFAULT 0 COMMENT 'T-7天提醒是否已发送',
    `remind_3d_sent` TINYINT NOT NULL DEFAULT 0 COMMENT 'T-3天提醒是否已发送',
    `remind_0d_sent` TINYINT NOT NULL DEFAULT 0 COMMENT 'T+0超时提醒是否已发送',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_next_deadline` (`next_deadline`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='心跳配置表';

-- 4.2 心跳打卡记录表
DROP TABLE IF EXISTS `heartbeat_record`;
CREATE TABLE `heartbeat_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `check_in_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '打卡时间',
    `check_in_type` VARCHAR(32) NOT NULL COMMENT '打卡方式：web/totp/recovery_code',
    `ip_address` VARCHAR(64) DEFAULT NULL COMMENT '打卡IP地址',
    `user_agent` VARCHAR(512) DEFAULT NULL COMMENT '设备信息',
    `totp_verified` TINYINT NOT NULL DEFAULT 1 COMMENT 'TOTP是否验证通过',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_check_in_at` (`check_in_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='心跳打卡记录表';

-- 4.3 提醒记录表
DROP TABLE IF EXISTS `remind_record`;
CREATE TABLE `remind_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `heartbeat_config_id` BIGINT NOT NULL COMMENT '心跳配置ID',
    `remind_type` VARCHAR(32) NOT NULL COMMENT '提醒类型：remind_14d/remind_7d/remind_3d/remind_0d',
    `channel` VARCHAR(32) NOT NULL COMMENT '渠道：email/sms/push/phone',
    `target` VARCHAR(128) NOT NULL COMMENT '发送目标（邮箱/手机号）',
    `send_status` TINYINT NOT NULL DEFAULT 0 COMMENT '发送状态：0-待发送 1-已发送 2-发送失败',
    `send_at` DATETIME DEFAULT NULL COMMENT '实际发送时间',
    `mock_data` TEXT DEFAULT NULL COMMENT 'Mock发送数据记录（JSON）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_send_status` (`send_status`),
    KEY `idx_heartbeat_config_id` (`heartbeat_config_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提醒记录表';

-- ============================================================
-- 五、触发验证引擎模块
-- ============================================================

-- 5.1 触发流程表
DROP TABLE IF EXISTS `trigger_process`;
CREATE TABLE `trigger_process` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `heartbeat_config_id` BIGINT NOT NULL COMMENT '心跳配置ID',
    `status` VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '流程状态：PENDING/T0_ALERT/T72_CONTACT_CHECK/T96_NOTARY/T120_FINAL_CONFIRM/COMPLETED/ABORTED',
    `grace_period_start` DATETIME DEFAULT NULL COMMENT 'T+0宽限期开始时间',
    `contact_check_start` DATETIME DEFAULT NULL COMMENT 'T+72h联系人核查开始时间',
    `notary_notify_at` DATETIME DEFAULT NULL COMMENT 'T+96h公证人通知时间',
    `final_confirm_at` DATETIME DEFAULT NULL COMMENT 'T+120h最终确认时间',
    `completed_at` DATETIME DEFAULT NULL COMMENT '流程完成时间',
    `aborted_at` DATETIME DEFAULT NULL COMMENT '流程中止时间',
    `abort_reason` VARCHAR(256) DEFAULT NULL COMMENT '中止原因',
    `abort_by` VARCHAR(32) DEFAULT NULL COMMENT '中止方：user/contact/recovery_code',
    `blockchain_tx_hash` VARCHAR(128) DEFAULT NULL COMMENT '区块链存证交易哈希（Mock）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='触发流程表';

-- 5.2 触发阶段记录表
DROP TABLE IF EXISTS `trigger_stage_record`;
CREATE TABLE `trigger_stage_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `trigger_process_id` BIGINT NOT NULL COMMENT '触发流程ID',
    `stage` VARCHAR(32) NOT NULL COMMENT '阶段：T0/T72/T96/T120',
    `action` VARCHAR(64) NOT NULL COMMENT '动作描述',
    `result` VARCHAR(32) NOT NULL COMMENT '结果：SUCCESS/FAILED/PENDING',
    `detail` TEXT DEFAULT NULL COMMENT '详细记录（JSON）',
    `mock_data` TEXT DEFAULT NULL COMMENT 'Mock数据（JSON）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_trigger_process_id` (`trigger_process_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='触发阶段记录表';

-- 5.3 可信联系人核查记录表
DROP TABLE IF EXISTS `contact_verification`;
CREATE TABLE `contact_verification` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `trigger_process_id` BIGINT NOT NULL COMMENT '触发流程ID',
    `trusted_contact_id` BIGINT NOT NULL COMMENT '可信联系人ID',
    `verification_status` TINYINT NOT NULL DEFAULT 0 COMMENT '核查状态：0-待回复 1-确认失联 2-确认活跃（中止）',
    `reply_at` DATETIME DEFAULT NULL COMMENT '回复时间',
    `reply_channel` VARCHAR(32) DEFAULT NULL COMMENT '回复渠道',
    `mock_data` TEXT DEFAULT NULL COMMENT 'Mock回复数据',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_trigger_process_id` (`trigger_process_id`),
    KEY `idx_trusted_contact_id` (`trusted_contact_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='可信联系人核查记录表';

-- ============================================================
-- 六、遗产交付系统模块
-- ============================================================

-- 6.1 交付链接表
DROP TABLE IF EXISTS `delivery_link`;
CREATE TABLE `delivery_link` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '原用户ID',
    `heir_id` BIGINT NOT NULL COMMENT '继承人人ID',
    `trigger_process_id` BIGINT NOT NULL COMMENT '触发流程ID',
    `jwt_token` VARCHAR(1024) NOT NULL COMMENT '一次性JWT票据',
    `link_token` VARCHAR(128) NOT NULL COMMENT '链接唯一标识（URL安全）',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-已失效 1-有效 2-已使用 3-已锁定',
    `expires_at` DATETIME NOT NULL COMMENT '过期时间（触发后7天）',
    `used_at` DATETIME DEFAULT NULL COMMENT '使用时间',
    `fail_count` INT NOT NULL DEFAULT 0 COMMENT '核验失败次数',
    `max_fail_count` INT NOT NULL DEFAULT 3 COMMENT '最大失败次数',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_link_token` (`link_token`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_heir_id` (`heir_id`),
    KEY `idx_trigger_process_id` (`trigger_process_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交付链接表';

-- 6.2 交付访问记录表
DROP TABLE IF EXISTS `delivery_access_log`;
CREATE TABLE `delivery_access_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `delivery_link_id` BIGINT NOT NULL COMMENT '交付链接ID',
    `heir_id` BIGINT NOT NULL COMMENT '继承人ID',
    `access_type` VARCHAR(32) NOT NULL COMMENT '访问类型：identity_check/decrypt/view',
    `ip_address` VARCHAR(64) DEFAULT NULL COMMENT '访问IP',
    `device_fingerprint` VARCHAR(128) DEFAULT NULL COMMENT '设备指纹',
    `user_agent` VARCHAR(512) DEFAULT NULL COMMENT '浏览器UA',
    `result` VARCHAR(16) NOT NULL COMMENT '结果：SUCCESS/FAILED',
    `blockchain_tx_hash` VARCHAR(128) DEFAULT NULL COMMENT '上链存证交易哈希（Mock）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_delivery_link_id` (`delivery_link_id`),
    KEY `idx_heir_id` (`heir_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交付访问记录表';

-- ============================================================
-- 七、通用模块
-- ============================================================

-- 7.1 操作审计日志表
DROP TABLE IF EXISTS `audit_log`;
CREATE TABLE `audit_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '操作用户ID',
    `module` VARCHAR(64) NOT NULL COMMENT '操作模块：auth/heartbeat/trigger/delivery/content',
    `action` VARCHAR(64) NOT NULL COMMENT '操作动作',
    `target_type` VARCHAR(64) DEFAULT NULL COMMENT '操作对象类型',
    `target_id` BIGINT DEFAULT NULL COMMENT '操作对象ID',
    `detail` TEXT DEFAULT NULL COMMENT '操作详情（JSON）',
    `ip_address` VARCHAR(64) DEFAULT NULL COMMENT 'IP地址',
    `user_agent` VARCHAR(512) DEFAULT NULL COMMENT '设备信息',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_module` (`module`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作审计日志表';

-- 7.2 套餐表
DROP TABLE IF EXISTS `plan`;
CREATE TABLE `plan` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(32) NOT NULL COMMENT '套餐名称：Free/Pro/Vault/Enterprise',
    `price_monthly` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '月费（美元）',
    `max_heirs` INT NOT NULL DEFAULT 1 COMMENT '最大继承人数',
    `max_storage_mb` INT NOT NULL DEFAULT 10 COMMENT '最大存储（MB）',
    `max_private_keys` INT NOT NULL DEFAULT 3 COMMENT '最大私钥条数',
    `max_passwords` INT NOT NULL DEFAULT 10 COMMENT '最大密码条数',
    `custom_check_in_period` TINYINT NOT NULL DEFAULT 0 COMMENT '是否支持自定义打卡周期',
    `notary_support` TINYINT NOT NULL DEFAULT 0 COMMENT '是否支持公证服务',
    `travel_mode` TINYINT NOT NULL DEFAULT 0 COMMENT '是否支持旅行模式',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-下架 1-上架',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='套餐表';

-- 7.3 验证码记录表（Mock短信/邮件验证码）
DROP TABLE IF EXISTS `verification_code`;
CREATE TABLE `verification_code` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `target` VARCHAR(128) NOT NULL COMMENT '发送目标（邮箱/手机号）',
    `code` VARCHAR(16) NOT NULL COMMENT '验证码',
    `code_type` VARCHAR(32) NOT NULL COMMENT '验证码类型：register/login/heir_confirm/delivery_check/totp_backup',
    `channel` VARCHAR(16) NOT NULL COMMENT '渠道：email/sms',
    `is_used` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已使用',
    `expire_at` DATETIME NOT NULL COMMENT '过期时间',
    `mock_data` TEXT DEFAULT NULL COMMENT 'Mock发送记录（JSON）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_target_code_type` (`target`, `code_type`),
    KEY `idx_expire_at` (`expire_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='验证码记录表';

-- 7.4 系统配置表
DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `config_key` VARCHAR(128) NOT NULL COMMENT '配置键',
    `config_value` TEXT NOT NULL COMMENT '配置值',
    `description` VARCHAR(256) DEFAULT NULL COMMENT '配置说明',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- ============================================================
-- 八、初始数据
-- ============================================================

-- 套餐初始数据
INSERT INTO `plan` (`name`, `price_monthly`, `max_heirs`, `max_storage_mb`, `max_private_keys`, `max_passwords`, `custom_check_in_period`, `notary_support`, `travel_mode`) VALUES
('Free', 0.00, 1, 10, 3, 10, 0, 0, 0),
('Pro', 19.00, 5, 1024, 999999, 999999, 1, 0, 1),
('Vault', 99.00, 999999, 10240, 999999, 999999, 1, 1, 1),
('Enterprise', 0.00, 999999, 102400, 999999, 999999, 1, 1, 1);

-- 系统配置初始数据
INSERT INTO `system_config` (`config_key`, `config_value`, `description`) VALUES
('default_check_in_period', '90', '默认打卡周期（天）'),
('grace_period_hours', '72', '超时后宽限期（小时）'),
('contact_check_hours', '24', '联系人核查窗口（小时）'),
('notary_notify_hours', '24', '公证人通知窗口（小时）'),
('delivery_link_expire_days', '7', '交付链接有效期（天）'),
('delivery_max_fail_count', '3', '交付核验最大失败次数'),
('heir_min_confirm_count', '2', '最少需要确认失联的联系人数量'),
('mock_mode_enabled', 'true', '全局Mock模式开关'),
('jwt_secret', 'LegacyVault_JWT_Secret_Key_2026_MOCK_ONLY', 'JWT签名密钥（Mock）'),
('jwt_expire_hours', '24', 'JWT有效期（小时）'),
('totp_issuer', 'LegacyVault', 'TOTP发行方名称'),
('pbkdf2_iterations', '310000', 'PBKDF2迭代次数');
