package com.hc.bookkeeping.common.base;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description: 懒加载树
 * @Date: 2020/7/20 8:43
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SimpleLazyTree<E, T> extends SimpleTree<E, T> {

    @ApiModelProperty(value = "是否有子节点")
    protected Boolean hasChildren;

}
