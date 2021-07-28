package com.hc.bookkeeping.modules.admin.dto;

import com.hc.bookkeeping.common.annotation.Query;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/7/1 9:13
 */
@Data
public class RoleQueryDto {
    @ApiModelProperty(value = "id")
    @Query
    private Long id;

    @ApiModelProperty(value = "角色名称")
    @Query(match = Query.Matching.INNER_LIKE)
    private String name;

    @ApiModelProperty(value = "描述")
    @Query(match = Query.Matching.INNER_LIKE)
    private String description;
}
