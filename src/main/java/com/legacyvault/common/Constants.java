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
    /** 继承人待确认 */
    public static final int HEIR_STATUS_PENDING = 0;
    /** 继承人已确认 */
    public static final int HEIR_STATUS_CONFIRMED = 1;
    /** 继承人已拒绝 */
    public static final int HEIR_STATUS_REJECTED = 2;

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
    /** 验证码 */
    public static final String REDIS_CODE_PREFIX = "code:";
    /** 心跳状态 */
    public static final String REDIS_HEARTBEAT_PREFIX = "heartbeat:";
    /** TOTP临时密钥 */
    public static final String REDIS_TOTP_TEMP_PREFIX = "totp:temp:";

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
}
