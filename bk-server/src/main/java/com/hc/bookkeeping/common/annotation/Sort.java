package com.hc.bookkeeping.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/7/7 10:49
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Sort {
    //匹配的数据库列
    String column() default "";

    //排序方式
    SortType sort() default SortType.ASC;

    //排序优先顺序
    int sortOrder() default 0;

    enum SortType{
        //升序
        ASC,
        //降序
        DESC
    }
}
