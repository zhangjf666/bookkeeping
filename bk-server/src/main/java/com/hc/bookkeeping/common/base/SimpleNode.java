package com.hc.bookkeeping.common.base;

import lombok.Data;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/8/4 11:20
 */
@Data
public class SimpleNode<T> implements Node<T> {

    /**
     * ID
     */
    protected T id;

    /**
     * 父节点ID
     */
    protected T pid;

    /**
     * 名称
     */
    protected CharSequence name;

    /**
     * 顺序 越小优先级越高 默认0
     */
    protected Comparable<?> weight = 0;
}
