package com.legacyvault.common;

/**
 * 全局常量定义
 *
 * @author LegacyVault
 */
public final class Constants {

    private Constants() {}

    // ==================== 用户状态 ====================
    /** 用户状态-禁用 */
    public static final int USER_STATUS_DISABLED = 0;
    /** 用户状态-正常 */
    public static final int USER_STATUS_NORMAL = 1;
    /** 用户状态-锁定 */
    public static final int USER_STATUS_LOCKED = 2;

    // ==================== TOTP ====================
    /** TOTP未绑定 */
    public static final int TOTP_NOT_BOUND = 0;
    /** TOTP已绑定 */
    public static final int TOTP_BOUND = 1;

    // ==================== KYC状态 ====================
    /** KYC未认证 */
    public static final int KYC_STATUS_NONE = 0;
    /** KYC已提交 */
    public static final int KYC_STATUS_SUBMITTED = 1;
    /** KYC已通过 */
    public static final int KYC_STATUS_PASSED = 2;
    /** KYC已拒绝 */
    public static final int KYC_STATUS_REJECTED = 3;

    // ==================== 继承人确认状态 ====================
    /** 继承人草稿（未发邀请） */
    public static final int HEIR_STATUS_DRAFT = 0;
    /** 继承人待确认（已发邀请） */
    public static final int HEIR_STATUS_PENDING = 1;
    /** 继承人已确认 */
    public static final int HEIR_STATUS_CONFIRMED = 2;
    /** 继承人已拒绝 */
    public static final int HEIR_STATUS_REJECTED = 3;

    // ==================== 注册步骤编号 ====================
    /** 注册步骤1：开户 */
    public static final int REG_STEP_ACCOUNT = 1;
    /** 注册步骤2：TOTP */
    public static final int REG_STEP_TOTP = 2;
    /** 注册步骤3：生物特征 */
    public static final int REG_STEP_BIOMETRIC = 3;
    /** 注册步骤4：KYC */
    public static final int REG_STEP_KYC = 4;
    /** 注册步骤5：恢复码 */
    public static final int REG_STEP_RECOVERY = 5;

    /** 步骤未完成 */
    public static final int STEP_NOT_DONE = 0;
    /** 步骤已完成 */
    public static final int STEP_DONE = 1;
    /** 步骤已跳过 */
    public static final int STEP_SKIPPED = 1;
    /** 步骤未跳过 */
    public static final int STEP_NOT_SKIPPED = 0;

    // ==================== KYC 单据状态 ====================
    /** KYC 单据-未提交 */
    public static final int KYC_RECORD_NONE = 0;
    /** KYC 单据-机审通过 */
    public static final int KYC_RECORD_AUTO_PASSED = 1;
    /** KYC 单据-机审失败 */
    public static final int KYC_RECORD_AUTO_FAILED = 2;
    /** KYC 单据-待人工审核 */
    public static final int KYC_RECORD_PENDING_MANUAL = 3;
    /** KYC 单据-人工通过 */
    public static final int KYC_RECORD_MANUAL_PASSED = 4;
    /** KYC 单据-人工驳回 */
    public static final int KYC_RECORD_MANUAL_REJECTED = 5;

    // ==================== 套餐类型 ====================
    /** Free 套餐 */
    public static final long PLAN_FREE = 1L;
    /** Pro 套餐 */
    public static final long PLAN_PRO = 2L;
    /** Vault 套餐 */
    public static final long PLAN_VAULT = 3L;

    /** Free 套餐继承人上限 */
    public static final int HEIR_LIMIT_FREE = 3;
    /** Pro 套餐继承人上限 */
    public static final int HEIR_LIMIT_PRO = 5;
    /** Vault 套餐继承人上限 */
    public static final int HEIR_LIMIT_VAULT = 10;

    // ==================== 内容类型 ====================
    /** 加密私钥 */
    public static final String CONTENT_TYPE_PRIVATE_KEY = "private_key";
    /** 账户密码 */
    public static final String CONTENT_TYPE_ACCOUNT_PASSWORD = "account_password";
    /** 遗言文本 */
    public static final String CONTENT_TYPE_LAST_WORDS = "last_words";
    /** 文件附件 */
    public static final String CONTENT_TYPE_FILE = "file";

    // ==================== 内容状态 ====================
    /** 内容已删除 */
    public static final int CONTENT_STATUS_DELETED = 0;
    /** 内容正常 */
    public static final int CONTENT_STATUS_NORMAL = 1;
    /** 内容已交付 */
    public static final int CONTENT_STATUS_DELIVERED = 2;

    // ==================== 触发流程状态 ====================
    /** 待触发 */
    public static final String TRIGGER_STATUS_PENDING = "PENDING";
    /** T+0超时告警 */
    public static final String TRIGGER_STATUS_T0_ALERT = "T0_ALERT";
    /** T+72h联系人核查 */
    public static final String TRIGGER_STATUS_T72_CONTACT_CHECK = "T72_CONTACT_CHECK";
    /** T+96h公证人通知 */
    public static final String TRIGGER_STATUS_T96_NOTARY = "T96_NOTARY";
    /** T+120h最终确认 */
    public static final String TRIGGER_STATUS_T120_FINAL = "T120_FINAL_CONFIRM";
    /** 已完成 */
    public static final String TRIGGER_STATUS_COMPLETED = "COMPLETED";
    /** 已中止 */
    public static final String TRIGGER_STATUS_ABORTED = "ABORTED";

    // ==================== 交付链接状态 ====================
    /** 链接已失效 */
    public static final int LINK_STATUS_INVALID = 0;
    /** 链接有效 */
    public static final int LINK_STATUS_VALID = 1;
    /** 链接已使用 */
    public static final int LINK_STATUS_USED = 2;
    /** 链接已锁定 */
    public static final int LINK_STATUS_LOCKED = 3;

    // ==================== 验证码类型 ====================
    /** 注册验证码 */
    public static final String CODE_TYPE_REGISTER = "register";
    /** 登录验证码 */
    public static final String CODE_TYPE_LOGIN = "login";
    /** 继承人确认验证码 */
    public static final String CODE_TYPE_HEIR_CONFIRM = "heir_confirm";
    /** 交付核验验证码 */
    public static final String CODE_TYPE_DELIVERY_CHECK = "delivery_check";

    // ==================== 提醒渠道 ====================
    /** 邮件 */
    public static final String CHANNEL_EMAIL = "email";
    /** 短信 */
    public static final String CHANNEL_SMS = "sms";
    /** App推送 */
    public static final String CHANNEL_PUSH = "push";
    /** 电话 */
    public static final String CHANNEL_PHONE = "phone";

    // ==================== Redis Key前缀 ====================
    /** 用户Token */
    public static final String REDIS_TOKEN_PREFIX = "token:user:";
    /** 管理员Token（与用户Token完全隔离） */
    public static final String REDIS_ADMIN_TOKEN_PREFIX = "token:admin:";
    /** 验证码 */
    public static final String REDIS_CODE_PREFIX = "code:";
    /** 心跳状态 */
    public static final String REDIS_HEARTBEAT_PREFIX = "heartbeat:";
    /** TOTP临时密钥 */
    public static final String REDIS_TOTP_TEMP_PREFIX = "totp:temp:";
    /** WebAuthn 挑战 */
    public static final String REDIS_WEBAUTHN_CHALLENGE_PREFIX = "webauthn:challenge:";

    // ==================== 审计模块 ====================
    /** 审计模块-认证 */
    public static final String AUDIT_MODULE_AUTH = "auth";
    /** 审计模块-心跳 */
    public static final String AUDIT_MODULE_HEARTBEAT = "heartbeat";
    /** 审计模块-触发 */
    public static final String AUDIT_MODULE_TRIGGER = "trigger";
    /** 审计模块-交付 */
    public static final String AUDIT_MODULE_DELIVERY = "delivery";
    /** 审计模块-内容 */
    public static final String AUDIT_MODULE_CONTENT = "content";
    /** 审计模块-继承人 */
    public static final String AUDIT_MODULE_HEIR = "heir";
    /** 审计模块-注册流程 */
    public static final String AUDIT_MODULE_REGISTRATION = "registration";
    /** 审计模块-KYC */
    public static final String AUDIT_MODULE_KYC = "kyc";
    /** 审计模块-生物特征 */
    public static final String AUDIT_MODULE_BIOMETRIC = "biometric";
    /** 审计模块-管理员 */
    public static final String AUDIT_MODULE_ADMIN = "admin";
    /** 审计模块-系统配置 */
    public static final String AUDIT_MODULE_SYS_CONFIG = "sys_config";

    // ==================== 管理员角色 ====================
    /** 普通管理员 */
    public static final String ADMIN_ROLE_ADMIN = "ADMIN";
    /** 超级管理员 */
    public static final String ADMIN_ROLE_SUPER_ADMIN = "SUPER_ADMIN";

    // ==================== 生物特征类型 ====================
    /** 人脸 */
    public static final String BIOMETRIC_TYPE_FACE = "FACE";
    /** 指纹 */
    public static final String BIOMETRIC_TYPE_FINGER = "FINGER";

    // ==================== 系统配置键 ====================
    /** 全局继承人绑定上限 */
    public static final String CONFIG_HEIR_GLOBAL_LIMIT = "heir.global_limit";
    /** KYC 触发资产阈值 */
    public static final String CONFIG_KYC_ASSET_THRESHOLD = "kyc.asset_threshold";
    /** KYC 服务商选择（MOCK / JUMIO / SUM_SUBSTANCE） */
    public static final String CONFIG_KYC_PROVIDER = "kyc.provider";
    /** 新加坡存储节点路径 */
    public static final String CONFIG_STORAGE_SG_PATH = "storage.sg_path";
}
