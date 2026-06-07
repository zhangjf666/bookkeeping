package com.hc.bookkeeping.modules.bkeeping.dto;

import com.hc.bookkeeping.common.annotation.Query;
import com.hc.bookkeeping.common.annotation.Sort;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户标签表查询
 * </p>
 *
 * @author zjf
 * @since 2023-06-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="UserTagQuery对象", description="用户标签表查询")
public class UserTagQueryDto implements Serializable {

    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "标签代码")
    @Query(column = "code")
    private Long code;

    @ApiModelProperty(value = "标签名称")
    @Query(column = "name", match = Query.Matching.INNER_LIKE)
    private String name;

    @ApiModelProperty(value = "用户id")
    @Query(column = "user_id")
    private Long userId;

    @ApiModelProperty(value = "标签颜色")
    @Query(column = "color")
    private String color;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    @Sort(column = "update_time", sort = Sort.SortType.DESC)
    private LocalDateTime updateTime;

}
