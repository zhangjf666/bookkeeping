package com.hc.bookkeeping.modules.admin.dto;

import com.hc.bookkeeping.common.annotation.Query;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/7/1 8:56
 */
@Data
public class MenuQueryDto {
    @ApiModelProperty(value = "id")
    @Query
    private Long id;

    @ApiModelProperty(value = "上级菜单id")
    @Query
    private Long pid;

    @ApiModelProperty(value = "名称")
    @Query(match = Query.Matching.INNER_LIKE)
    private String name;
}
