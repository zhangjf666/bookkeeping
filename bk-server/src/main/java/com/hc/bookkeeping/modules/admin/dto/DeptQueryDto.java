package com.hc.bookkeeping.modules.admin.dto;

import com.hc.bookkeeping.common.annotation.Query;
import com.hc.bookkeeping.common.annotation.Sort;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/7/1 9:06
 */
@Data
public class DeptQueryDto {

    @ApiModelProperty(value = "id")
    @Query
    private Long id;

    @ApiModelProperty(value = "名称")
    @Query(match = Query.Matching.INNER_LIKE)
    private String name;

    @ApiModelProperty(value = "上级部门id")
    @Query
    private Long pid;

    @ApiModelProperty(value = "状态(1:启用 0:禁用)")
    @Query
    private String enabled;

    @ApiModelProperty(value = "排序号")
    @Sort
    private String sort;
}
