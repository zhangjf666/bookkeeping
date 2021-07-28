package com.hc.bookkeeping.common.model;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ResponseCode implements IResponseCode {
    /** 成功 */
    OK(0,"ok"),
    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    PAYMENT_REQUIRED(402, "Payment Required"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    DATA_NOT_EXSIT(900, "data not exsit");


    private int code;

    private String msg;

    @Override
    public int getCode() {
        return this.code;
    }

    @Override
    public String getMsg() {
        return this.msg;
    }
}
