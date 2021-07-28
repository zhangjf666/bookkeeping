package com.hc.bookkeeping.common.exception;

import com.hc.bookkeeping.common.model.IResponseCode;
import com.hc.bookkeeping.common.model.ResponseCode;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/6/9 10:22
 */
public class DataNotExsitException extends BaseException {
    private Integer errorCode = ResponseCode.DATA_NOT_EXSIT.getCode();

    public DataNotExsitException() {
        this(ResponseCode.DATA_NOT_EXSIT.getMsg());
    }

    public DataNotExsitException(IResponseCode code) {
        this(code,code.getMsg());
    }

    public DataNotExsitException(IResponseCode code, String message) {
        super(message);
        this.errorCode = code.getCode();
    }

    public DataNotExsitException(String message) {
        super(message);
    }

    public DataNotExsitException(IResponseCode code, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = code.getCode();
    }

    public DataNotExsitException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataNotExsitException(IResponseCode code, Throwable cause) {
        super(code.getMsg(),cause);
        this.errorCode = code.getCode();
    }

    public DataNotExsitException(Throwable cause) {
        super(cause);
    }

    @Override
    public Integer getErrorCode() {
        return errorCode;
    }
}
