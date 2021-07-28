package com.hc.bookkeeping.common.exception;

import com.hc.bookkeeping.common.model.IResponseCode;
import com.hc.bookkeeping.common.model.ResponseCode;

/**
 * @Author: zhangjunfeng
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: Created on 2017/10/12 9:39
 */
public class BusinessException extends BaseException {
    private Integer errorCode = ResponseCode.INTERNAL_SERVER_ERROR.getCode();

    private BusinessException() {
    }

    public BusinessException(IResponseCode code) {
        this(code,code.getMsg());
    }

    public BusinessException(IResponseCode code, String message) {
        super(message);
        this.errorCode = code.getCode();
    }

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(IResponseCode code, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = code.getCode();
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    public BusinessException(IResponseCode code, Throwable cause) {
        super(code.getMsg(),cause);
        this.errorCode = code.getCode();
    }

    public BusinessException(Throwable cause) {
        super(cause);
    }

    @Override
    public Integer getErrorCode() {
        return this.errorCode;
    }
}
