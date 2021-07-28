package com.hc.bookkeeping.common.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @Author: zhangjunfeng
 * @Email: junfeng1987@163.com
 * @Description: 请求返回结果
 * @Date: Created on 2017/10/11.11:07
 */
@Getter
@Setter
public final class Response<T> implements Serializable {
    private int code;
    private String msg;
    private T data = null;
    private long timestamp;

    private Response() {
    }

    public Response(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    public Response(int code, String msg) {
        this.code = code;
        this.msg = msg;
        this.data = null;
        this.timestamp = System.currentTimeMillis();
    }

    public Response(IResponseCode responseCode, T data) {
        this(responseCode.getCode(), responseCode.getMsg(), data);
    }

    public static Response ok() {
        return ok(null);
    }

    public static<T> Response ok(T data) {
        return new Response<T>(ResponseCode.OK, data);
    }

    public static<T> Response fail(IResponseCode responseCode, T data){
        return new Response<T>(responseCode.getCode(),responseCode.getMsg(),data);
    }

    public static Response fail(IResponseCode responseCode){
        return new Response(responseCode.getCode(),responseCode.getMsg());
    }

    public static<T> Response fail(int code, String msg, T data){
        return new Response<T>(code,msg,data);
    }

    public static Response fail(int code, String msg){
        return new Response(code,msg);
    }
}
