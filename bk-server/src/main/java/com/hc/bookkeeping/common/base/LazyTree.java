package com.hc.bookkeeping.common.base;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/8/19 10:19
 */
public interface LazyTree<E, T> extends Tree<E, T> {

    /**
     * 设置是否有子节点
     *
     * @param hasChildren 是否有子节点
     */
    default void setHasChildren(Boolean hasChildren){

    }

    /**
     * 是否叶子节点
     *
     * @return 是否叶子节点
     */
    default boolean isLeaf(){
        return getChildren() == null || getChildren().size() <= 0;
    }

    /**
     * 是否有子节点
     *
     * @return 是否有子节点
     */
    default Boolean getHasChildren(){
        return getChildren() != null && getChildren().size() > 0;
    }
}
