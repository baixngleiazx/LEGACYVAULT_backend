# LegacyVault 后端服务

## 技术栈
- Java 1.8 + Spring Boot 2.7.18
- MyBatis-Plus 3.5.5
- MySQL 8.0
- Redis
- JWT 认证

## 快速启动

### 1. 环境准备
```bash
# JDK 1.8
java -version

# MySQL 8.0
mysql --version

# Redis
redis-cli ping
```

### 2. 初始化数据库
```bash
mysql -u root -p < sql/init.sql
```

### 3. 修改配置
编辑 `src/main/resources/application.yml`：
- 修改数据库连接（用户名/密码）
- 修改Redis连接（如有密码）

### 4. 启动后端
```bash
# Maven编译并启动
mvn clean package -DskipTests
java -jar target/legacy-vault-backend-1.0.0-SNAPSHOT.jar

# 或者开发模式
mvn spring-boot:run
```

### 5. 验证
```bash
curl http://localhost:8080/api/auth/send-code -X POST \
  -H "Content-Type: application/json" \
  -d '{"target":"test@example.com","codeType":"register","channel":"email"}'
```

## Mock模式说明
所有外部接口默认使用Mock模式：
- 短信/邮件 → 仅记录日志，不实际发送
- KYC核验 → 直接返回通过
- 人脸识别 → 直接返回通过
- 区块链存证 → 生成模拟交易哈希
- 文件存储 → 使用本地文件系统

切换正式环境：修改 `application.yml` 中 `legacy-vault.mock-mode-enabled: false`

## API接口总览
| 模块 | 路径前缀 | 说明 |
|------|---------|------|
| 认证 | /api/auth | 注册、登录、TOTP、恢复码 |
| 用户 | /api/user | 用户信息、KYC |
| 心跳 | /api/heartbeat | 打卡、周期设置、旅行模式 |
| 内容 | /api/content | 加密内容CRUD |
| 继承人 | /api/heir | 继承人管理 |
| 可信联系人 | /api/trusted-contact | 可信联系人管理 |
| 触发 | /api/trigger | 触发流程查询/中止 |
| 交付 | /api/delivery | 交付链接、身份核验 |
