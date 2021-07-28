package com.hc.bookkeeping.common.annotation;

import com.hc.bookkeeping.common.model.LogType;

import java.lang.annotation.*;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description: 日志类型
 * @Date: 2020/6/8 13:52
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {

    String value();

    LogType type() default LogType.OPERATION;
}
