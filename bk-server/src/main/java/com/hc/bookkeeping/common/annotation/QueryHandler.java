package com.hc.bookkeeping.common.annotation;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 * 自定义查询处理器接口
 * 用于处理复杂的查询条件
 */
public interface QueryHandler {

    /**
     * 应用自定义查询条件
     * @param wrapper 查询包装器
     * @param value 字段值
     */
    void apply(QueryWrapper<?> wrapper, Object value);
}