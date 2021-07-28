package com.hc.bookkeeping.modules.admin.dto;

import com.hc.bookkeeping.common.annotation.Query;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/6/30 10:57
 */
@Data
public class DictQueryDto {
    @ApiModelProperty(value = "id")
    @Query
    private Long id;

    @ApiModelProperty(value = "字典名称")
    @Query(match = Query.Matching.INNER_LIKE)
    private String name;

    @ApiModelProperty(value = "字典描述")
    @Query(match = Query.Matching.INNER_LIKE)
    private String description;

    @ApiModelProperty(value = "字典类型")
    @Query
    private String type;
}
