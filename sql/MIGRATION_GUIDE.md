# LegacyVault v1.1 数据库迁移指南

> 功能：手机号注册 / 验证码登录（手机号 + 邮箱）
> 日期：2026-06-14

---

## 一、结论速览

**本次代码修改未引入任何新表或新字段**，完全复用了 v1.0 `init.sql` 中已有的表结构：

| 表名 | 关键字段 | 是否在 v1.0 已存在 |
|---|---|---|
| `user` | `phone` VARCHAR(32) | ✅ 已存在 |
| `user` | `uk_phone` 唯一索引 | ✅ 已存在 |
| `verification_code` | `code_type` VARCHAR(32) | ✅ 已存在 |
| `verification_code` | `channel` VARCHAR(16) | ✅ 已存在 |

---

## 二、部署方式

### 场景 A：全新部署（新数据库）

**无需执行任何迁移脚本**，直接执行：

```bash
mysql -u <用户名> -p < LEGACY_VAULT-backend/sql/init.sql
```

`init.sql` 已包含所有必需字段和索引。

---

### 场景 B：从 v1.0 init.sql 部署的数据库升级

**无需执行迁移**，v1.0 的 `init.sql` 已包含本次所需全部结构。

直接启动后端服务即可使用新功能。

---

### 场景 C：从更早版本（v1.0 之前）的数据库升级

需执行迁移脚本：

```bash
mysql -u <用户名> -p legacy_vault < LEGACY_VAULT-backend/sql/migration_v1.1_phone_verification.sql
```

**脚本包含：**

1. **前置检查**：查询当前数据库是否缺少字段/索引
2. **增量变更**：按需执行的 ALTER TABLE 语句（默认被注释，按检查结果取消注释执行）
3. **数据清洗**：检查现有数据是否满足唯一性约束
4. **验证查询**：确认变更后的表结构
5. **回滚脚本**：如需撤销变更

---

## 三、快速检查命令

部署完成后，执行以下 SQL 确认表结构正确：

```sql
USE legacy_vault;

-- 1. 确认 user 表有 phone 字段
SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'legacy_vault'
  AND TABLE_NAME = 'user'
  AND COLUMN_NAME = 'phone';
-- 期望：phone | varchar | 32

-- 2. 确认 user 表有 phone 唯一索引
SELECT INDEX_NAME, COLUMN_NAME, NON_UNIQUE
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'legacy_vault'
  AND TABLE_NAME = 'user'
  AND INDEX_NAME = 'uk_phone';
-- 期望：uk_phone | phone | 0（0 表示唯一）

-- 3. 确认 verification_code 表结构完整
DESC verification_code;
-- 期望包含：id, target, code, code_type, channel, is_used, expire_at, mock_data, created_at
```

---

## 四、涉及的业务逻辑（无需 SQL 变更，代码已自动处理）

| 功能 | 实现位置 | 说明 |
|---|---|---|
| 手机号注册 | `AuthServiceImpl.register()` | 检查 `user.phone` 唯一性 |
| 邮箱注册 | `AuthServiceImpl.register()` | 沿用原逻辑 |
| 发送验证码（手机/邮箱自动识别） | `AuthServiceImpl.sendVerifyCode()` | 根据 target 自动选择 sms/email 渠道 |
| 验证码登录（手机/邮箱） | `AuthServiceImpl.loginByCode()` | 按 target 类型查 email 或 phone |
| 验证码频率限制（60秒） | `AuthServiceImpl.sendVerifyCode()` | Redis `code:rate_limit:{target}` |
| 验证码有效期（5分钟） | `AuthServiceImpl.sendVerifyCode()` | Redis TTL + DB `expire_at` |

---

## 五、Mock 验证码获取方式

当前为 Mock 模式，验证码不真实发送，可通过以下方式获取：

1. **后端控制台日志**：搜索 `【Mock短信】` 或 `【Mock邮件】` 关键字
2. **数据库查询**：
   ```sql
   SELECT target, code, code_type, channel, expire_at, created_at
   FROM verification_code
   WHERE is_used = 0 AND expire_at > NOW()
   ORDER BY created_at DESC
   LIMIT 10;
   ```
3. **Redis 查询**：
   ```bash
   redis-cli GET "code:register:<手机号或邮箱>"
   redis-cli GET "code:login:<手机号或邮箱>"
   ```

---

## 六、回滚说明

如需回滚到不支持手机号注册的版本：

1. **代码回滚**：`git revert` 本次提交
2. **数据库**：无需变更（phone 字段保留为 NULL 即可，不影响原有功能）
3. **如必须删除字段**：参考迁移脚本第六节回滚 SQL（谨慎操作）

---

## 七、脚本文件清单

| 文件 | 用途 | 执行时机 |
|---|---|---|
| `sql/init.sql` | 完整建库脚本 | 全新部署 |
| `sql/mock_data.sql` | Mock 测试数据 | 开发测试环境 |
| `sql/migration_v1.1_phone_verification.sql` | 本版本迁移脚本 | 从旧版本升级时 |
