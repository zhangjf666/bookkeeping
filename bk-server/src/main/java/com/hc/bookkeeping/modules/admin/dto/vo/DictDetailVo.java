package com.hc.bookkeeping.modules.admin.dto.vo;

import com.hc.bookkeeping.common.base.SimpleLazyTree;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/8/5 11:07
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DictDetailVo extends SimpleLazyTree<DictDetailVo, Long> {
    @ApiModelProperty(value = "字典id")
    private Long dictId;

    @ApiModelProperty(value = "数据值")
    private String value;

    @ApiModelProperty(value = "数据名称")
    private String name;

    @ApiModelProperty(value = "排序")
    private Integer sort;

    public void setSort(Integer sort) {
        this.sort = sort;
        this.weight = sort;
    }
}
