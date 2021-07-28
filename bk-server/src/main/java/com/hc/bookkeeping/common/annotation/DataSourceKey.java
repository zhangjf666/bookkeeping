package com.hc.bookkeeping.common.annotation;

import java.lang.annotation.*;

/**
 * @Author: zhangjunfeng
 * @Email: junfeng1987@163.com
 * @Description: 手动选择数据源注解,name为数据源名称
 * @Date: Created on 2018/12/18 15:39
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataSourceKey {
    String name();
}
