-- ============================================================
-- LegacyVault 测试数据脚本
-- 初始化测试用户和示例数据
-- ============================================================
USE legacy_vault;

-- 测试用户（密码: test123456）
INSERT INTO `user` (`email`, `phone`, `password_hash`, `nickname`, `status`, `plan_id`, `totp_bound`, `kyc_status`, `security_score`, `travel_mode_enabled`)
VALUES ('test@legacyvault.com', '13800138000', 'a1b2c3d4e5f6g7h8:9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08', '测试用户', 1, 2, 0, 0, 0, 0);

-- 测试心跳配置
INSERT INTO `heartbeat_config` (`user_id`, `check_in_period_days`, `next_deadline`, `remind_14d_sent`, `remind_7d_sent`, `remind_3d_sent`, `remind_0d_sent`)
SELECT id, 90, DATE_ADD(NOW(), INTERVAL 90 DAY), 0, 0, 0, 0 FROM `user` WHERE email = 'test@legacyvault.com';

-- 测试加密内容
INSERT INTO `encrypted_content` (`user_id`, `content_type`, `title`, `encrypted_data`, `content_hash`, `status`)
SELECT id, 'private_key', 'BTC钱包助记词', 'TW9ja0VuY3J5cHRlZERhdGE=', 'abcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890', 1 FROM `user` WHERE email = 'test@legacyvault.com';

INSERT INTO `encrypted_content` (`user_id`, `content_type`, `title`, `encrypted_data`, `content_hash`, `status`)
SELECT id, 'account_password', '交易所账号', 'TW9ja0VuY3J5cHRlZERhdGEy', '1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef', 1 FROM `user` WHERE email = 'test@legacyvault.com';

INSERT INTO `encrypted_content` (`user_id`, `content_type`, `title`, `encrypted_data`, `content_hash`, `status`)
SELECT id, 'last_words', '给家人的话', 'TW9ja0xhc3RXb3Jkcw==', 'fedcba0987654321fedcba0987654321fedcba0987654321fedcba0987654321', 1 FROM `user` WHERE email = 'test@legacyvault.com';

-- 测试继承人
INSERT INTO `heir` (`user_id`, `name`, `email`, `phone`, `confirmation_status`, `confirmation_token`, `assigned_content_count`)
SELECT id, '张三', 'heir1@example.com', '13900139001', 1, 'mock_token_001', 3 FROM `user` WHERE email = 'test@legacyvault.com';

INSERT INTO `heir` (`user_id`, `name`, `email`, `phone`, `confirmation_status`, `confirmation_token`, `assigned_content_count`)
SELECT id, '李四', 'heir2@example.com', '13900139002', 0, 'mock_token_002', 0 FROM `user` WHERE email = 'test@legacyvault.com';

-- 测试可信联系人
INSERT INTO `trusted_contact` (`user_id`, `name`, `email`, `phone`, `relationship`)
SELECT id, '王五', 'contact1@example.com', '13700137001', 'friend' FROM `user` WHERE email = 'test@legacyvault.com';

INSERT INTO `trusted_contact` (`user_id`, `name`, `email`, `phone`, `relationship`)
SELECT id, '赵六', 'contact2@example.com', '13700137002', 'family' FROM `user` WHERE email = 'test@legacyvault.com';

INSERT INTO `trusted_contact` (`user_id`, `name`, `email`, `phone`, `relationship`)
SELECT id, '律师陈', 'lawyer@example.com', '13600136001', 'lawyer' FROM `user` WHERE email = 'test@legacyvault.com';
