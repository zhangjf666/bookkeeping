package com.hc.bookkeeping.modules.admin.dto;

import com.hc.bookkeeping.common.annotation.Query;
import com.hc.bookkeeping.common.annotation.Sort;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/6/30 16:38
 */
@Data
public class DictDetailQueryDto {
    @ApiModelProperty(value = "字典id")
    @Query(column = "dict_id")
    private Long dictId;

    @ApiModelProperty(value = "上级id")
    @Query
    private Long pid;

    @ApiModelProperty(value = "排序")
    @Sort
    private String sort;
}
