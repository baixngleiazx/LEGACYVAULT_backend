-- ============================================================
-- LegacyVault 增量迁移脚本
-- 版本：v1.1 手机号注册 / 验证码登录
-- 日期：2026-06-14
-- 说明：本次代码修改未新增表或字段（user.phone 与
--       verification_code 表在 v1.0 init.sql 中已存在）。
--       本脚本用于兼容早期数据库版本升级。
-- ============================================================

-- 使用数据库
USE legacy_vault;

-- ============================================================
-- 一、前置检查（先执行这些查询，确认是否需要后续操作）
-- ============================================================

-- 1.1 检查 user 表是否存在 phone 字段
SELECT COUNT(*) AS phone_column_exists
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'legacy_vault'
  AND TABLE_NAME = 'user'
  AND COLUMN_NAME = 'phone';
-- 预期结果：1 = 已存在，无需执行步骤 二.1
--          0 = 不存在，需执行步骤 二.1

-- 1.2 检查 user 表是否存在 phone 唯一索引
SELECT COUNT(*) AS uk_phone_exists
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'legacy_vault'
  AND TABLE_NAME = 'user'
  AND INDEX_NAME = 'uk_phone';
-- 预期结果：>0 = 已存在，无需执行步骤 二.2
--           0 = 不存在，需执行步骤 二.2

-- 1.3 检查 verification_code 表是否存在 channel 字段
SELECT COUNT(*) AS channel_column_exists
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'legacy_vault'
  AND TABLE_NAME = 'verification_code'
  AND COLUMN_NAME = 'channel';
-- 预期结果：1 = 已存在，无需执行步骤 二.3

-- 1.4 检查 verification_code 表是否存在 code_type 字段
SELECT COUNT(*) AS code_type_column_exists
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'legacy_vault'
  AND TABLE_NAME = 'verification_code'
  AND COLUMN_NAME = 'code_type';
-- 预期结果：1 = 已存在，无需执行步骤 二.4


-- ============================================================
-- 二、增量变更（根据前置检查结果按需执行）
-- ============================================================

-- 2.1 为 user 表新增 phone 字段（如 1.1 检查为 0 则执行）
-- ALTER TABLE `user`
--     ADD COLUMN `phone` VARCHAR(32) DEFAULT NULL COMMENT '手机号' AFTER `email`;

-- 2.2 为 user 表 phone 字段新增唯一索引（如 1.2 检查为 0 则执行）
-- 注意：执行前必须确认无重复手机号数据，否则需先清洗
-- ALTER TABLE `user`
--     ADD UNIQUE KEY `uk_phone` (`phone`);

-- 2.3 为 verification_code 表新增 channel 字段（如 1.3 检查为 0 则执行）
-- ALTER TABLE `verification_code`
--     ADD COLUMN `channel` VARCHAR(16) NOT NULL DEFAULT 'email' COMMENT '渠道：email/sms' AFTER `code_type`;

-- 2.4 为 verification_code 表新增 code_type 字段（如 1.4 检查为 0 则执行）
-- ALTER TABLE `verification_code`
--     ADD COLUMN `code_type` VARCHAR(32) NOT NULL DEFAULT 'register' COMMENT '验证码类型：register/login/heir_confirm/delivery_check' AFTER `code`;


-- ============================================================
-- 三、数据清洗（升级旧库时才可能需要）
-- ============================================================

-- 3.1 如果已有用户数据，需确认 phone 字段唯一性
-- 查询是否存在重复手机号（理论上不应存在）
SELECT phone, COUNT(*) AS cnt
FROM `user`
WHERE phone IS NOT NULL AND deleted = 0
GROUP BY phone
HAVING cnt > 1;
-- 预期结果：0 行。如有结果，需先合并或清理重复账号

-- 3.2 查询已占用手机号的用户列表（用于人工核查）
SELECT id, email, phone, nickname, status, created_at
FROM `user`
WHERE phone IS NOT NULL AND deleted = 0
ORDER BY created_at DESC;


-- ============================================================
-- 四、验证（执行完变更后的校验）
-- ============================================================

-- 4.1 确认 user 表结构完整
DESC `user`;
-- 应包含：id, email, phone, password_hash, nickname, status,
--         plan_id, plan_expires_at, totp_bound, biometric_bound,
--         kyc_status, security_score, travel_mode_enabled,
--         travel_start_date, travel_end_date, last_login_at,
--         created_at, updated_at, deleted

-- 4.2 确认 verification_code 表结构完整
DESC `verification_code`;
-- 应包含：id, target, code, code_type, channel, is_used,
--         expire_at, mock_data, created_at

-- 4.3 确认索引
SHOW INDEX FROM `user`;
-- 应包含：PRIMARY, uk_email, uk_phone, idx_status

SHOW INDEX FROM `verification_code`;
-- 应包含：PRIMARY, idx_target_code_type, idx_expire_at


-- ============================================================
-- 五、全新部署说明
-- ============================================================
-- 如果是全新数据库，直接执行 init.sql 即可，无需执行本迁移脚本。
-- init.sql 已包含：
--   - user 表的 phone 字段和 uk_phone 唯一索引
--   - verification_code 表的完整字段（含 code_type、channel）
--
-- 执行命令：
--   mysql -u <用户名> -p < legacy_vault_backend/sql/init.sql
--
-- 本迁移脚本仅用于从旧版本数据库升级到支持手机号注册/登录的版本。


-- ============================================================
-- 六、回滚脚本（如需撤销本次变更）
-- ============================================================

-- 6.1 删除 user 表的 phone 唯一索引
-- ALTER TABLE `user` DROP INDEX `uk_phone`;

-- 6.2 删除 user 表的 phone 字段
-- 注意：删除字段前请确认无业务依赖，且已备份数据
-- ALTER TABLE `user` DROP COLUMN `phone`;

-- 6.3 删除 verification_code 表的 channel 字段
-- ALTER TABLE `verification_code` DROP COLUMN `channel`;

-- 6.4 删除 verification_code 表的 code_type 字段
-- ALTER TABLE `verification_code` DROP COLUMN `code_type`;


-- ============================================================
-- 七、本次代码修改对应的 SQL 影响总结
-- ============================================================
--
-- 表名                | 操作      | 说明
-- ------------------- | --------- | ----------------------------------
-- user                | 无新增    | phone 字段已存在于 init.sql
-- user                | 无新增    | uk_phone 唯一索引已存在
-- verification_code   | 无新增    | 字段已满足需求
--
-- 说明：本次代码修改（手机号注册、验证码登录）完全复用了
--       v1.0 init.sql 中已有的表结构和字段，未引入任何新表或新字段。
--       仅当从更早版本（v1.0 之前）升级时才需要执行本脚本中的 ALTER 语句。
-- ============================================================
