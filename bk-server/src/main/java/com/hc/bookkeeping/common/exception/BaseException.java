package com.hc.bookkeeping.common.exception;

/**
 * @Author: zhangjunfeng
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: Created on 2017/10/12 9:25
 */
public abstract class BaseException extends RuntimeException {
    public BaseException() {
    }

    public BaseException(String message) {
        super(message);
    }

    public BaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public BaseException(Throwable cause) {
        super(cause);
    }

    public abstract Integer getErrorCode();
}
