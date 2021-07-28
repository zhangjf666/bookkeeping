package com.hc.bookkeeping.common.base;

import java.util.List;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/8/5 11:11
 */
public interface Tree<E, T> extends Node<T> {
    /**
     * 获取子节点列表
     *
     * @return 子节点列表
     */
    List<E> getChildren();


    /**
     * 设置子节点
     *
     * @param children 子节点列表
     */
    void setChildren(List<E> children);
}
