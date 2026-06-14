package com.legacyvault.exception;

import com.legacyvault.common.ResultCode;
import lombok.Getter;

/**
 * 业务异常类
 * 所有业务逻辑异常统一使用此类抛出
 *
 * @author LegacyVault
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 错误码 */
    private final Integer code;

    /** 错误信息 */
    private final String message;

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.ERROR.getCode();
        this.message = message;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }

    public BusinessException(ResultCode resultCode, String detail) {
        super(resultCode.getMessage() + "：" + detail);
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage() + "：" + detail;
    }
}
