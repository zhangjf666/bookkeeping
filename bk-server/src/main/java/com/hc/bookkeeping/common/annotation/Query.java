package com.hc.bookkeeping.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description: 查询条件构建
 * @Date: 2020/6/29 9:29
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Query {

    //匹配的数据库列
    String column() default "";

    //查询类型,默认匹配方式
    Type type() default Type.MATCHING;

    //原始sql
    String sql() default "";

    //匹配类型,默认相等
    Matching match() default Matching.EQUAL;

    enum Matching {
        //相等
        EQUAL
        //大于
        , GREATER_THAN
        //小于
        , LESS_THAN
        //左右模糊
        , INNER_LIKE
        //左模糊
        , LEFT_LIKE
        //右模糊
        , RIGHT_LIKE
        //小于等于
        , LESS_EQ
        //大于等于
        , GREATER_EQ
        //包含
        , IN
        //不相等
        ,NOT_EQUAL
        //between
        ,BETWEEN
        //not between
        ,NOT_BETWEEN
        //不为空
        ,NOT_NULL
        //为空
        ,IS_NULL
    }

    //查询方式
    enum Type{
        //匹配
        MATCHING,
        //使用原始sql
        SQL
    }
}
