package com.hc.bookkeeping.modules.admin.dto;

import com.hc.bookkeeping.common.annotation.Query;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/6/29 8:37
 */
@Data
public class PositionQueryDto {

    @ApiModelProperty(value = "id")
    @Query
    private Long id;

    @ApiModelProperty(value = "名称")
    @Query(match = Query.Matching.LEFT_LIKE)
    private String name;

    @ApiModelProperty(value = "描述")
    @Query(match = Query.Matching.RIGHT_LIKE)
    private String description;

    @ApiModelProperty(value = "英文名称")
    @Query(type = Query.Type.SQL,sql = "name_en like {0}")
    private String nameEn;
}
