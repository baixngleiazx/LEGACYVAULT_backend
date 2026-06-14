package com.legacyvault.common;

import lombok.Getter;

/**
 * 统一返回状态码枚举
 *
 * @author LegacyVault
 */
@Getter
public enum ResultCode {

    // ==================== 通用状态码 ====================
    SUCCESS(200, "操作成功"),
    ERROR(500, "服务器内部错误"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或Token已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),

    // ==================== 认证模块 1xxx ====================
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户已存在"),
    PASSWORD_ERROR(1003, "密码错误"),
    VERIFY_CODE_ERROR(1004, "验证码错误或已过期"),
    VERIFY_CODE_EXPIRED(1005, "验证码已过期"),
    TOTP_NOT_BOUND(1006, "TOTP未绑定"),
    TOTP_VERIFY_ERROR(1007, "TOTP验证失败"),
    TOTP_ALREADY_BOUND(1008, "TOTP已绑定"),
    TOKEN_INVALID(1009, "Token无效"),
    ACCOUNT_DISABLED(1010, "账户已被禁用"),
    ACCOUNT_LOCKED(1011, "账户已被锁定"),
    RECOVERY_CODE_ERROR(1012, "恢复码错误"),
    RECOVERY_CODE_USED(1013, "恢复码已使用"),
    PHONE_FORMAT_ERROR(1014, "手机号格式不正确"),
    PHONE_ALREADY_EXISTS(1015, "手机号已被注册"),
    CODE_SEND_TOO_FREQUENT(1016, "发送过于频繁，请稍后再试"),
    TARGET_FORMAT_ERROR(1017, "目标格式不正确，请输入手机号或邮箱"),

    // ==================== 内容模块 2xxx ====================
    CONTENT_NOT_FOUND(2001, "内容不存在"),
    CONTENT_LIMIT_EXCEEDED(2002, "内容数量超出套餐限制"),
    STORAGE_LIMIT_EXCEEDED(2003, "存储空间不足"),
    CONTENT_ALREADY_DELETED(2004, "内容已被删除"),
    ENCRYPT_ERROR(2005, "加密操作失败"),

    // ==================== 心跳模块 3xxx ====================
    HEARTBEAT_CONFIG_NOT_FOUND(3001, "心跳配置不存在"),
    ALREADY_CHECKED_IN(3002, "今日已打卡"),
    TRAVEL_MODE_CONFLICT(3003, "旅行模式设置冲突"),

    // ==================== 触发模块 4xxx ====================
    TRIGGER_PROCESS_NOT_FOUND(4001, "触发流程不存在"),
    TRIGGER_ALREADY_ACTIVE(4002, "触发流程已在进行中"),
    TRIGGER_ALREADY_ABORTED(4003, "触发流程已中止"),
    CONTACT_NOT_ENOUGH(4004, "确认失联的联系人数量不足"),

    // ==================== 交付模块 5xxx ====================
    DELIVERY_LINK_EXPIRED(5001, "交付链接已过期"),
    DELIVERY_LINK_USED(5002, "交付链接已使用"),
    DELIVERY_LINK_LOCKED(5003, "交付链接已锁定，请联系客服"),
    DELIVERY_IDENTITY_VERIFY_FAILED(5004, "身份核验失败"),
    DELIVERY_LINK_INVALID(5005, "交付链接无效"),

    // ==================== 继承人模块 6xxx ====================
    HEIR_LIMIT_EXCEEDED(6001, "继承人数量超出套餐限制"),
    HEIR_NOT_FOUND(6002, "继承人不存在于"),
    HEIR_ALREADY_CONFIRMED(6003, "继承人已确认"),
    HEIR_CONFIRM_TOKEN_INVALID(6004, "确认邀请Token无效");

    /** 状态码 */
    private final Integer code;

    /** 提示信息 */
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
