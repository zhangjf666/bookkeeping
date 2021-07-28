package com.hc.bookkeeping.common.base;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/8/19 10:05
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SimpleTree<E, T> extends SimpleNode<T> implements LazyTree<E, T> {

    @ApiModelProperty(value = "子节点列表")
    protected List<E> children;
}
