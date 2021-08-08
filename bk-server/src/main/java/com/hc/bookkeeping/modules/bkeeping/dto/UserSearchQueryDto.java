package com.hc.bookkeeping.modules.bkeeping.dto;

import com.hc.bookkeeping.common.annotation.Query;
import com.hc.bookkeeping.common.annotation.Sort;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="UserSearch对象", description="用户搜索记录表")
public class UserSearchQueryDto {
    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "用户id")
    @Query(column = "user_id")
    private Long userId;

    @ApiModelProperty(value = "搜索内容")
    @Query(match = Query.Matching.INNER_LIKE)
    private String content;

    @ApiModelProperty(value = "创建时间")
    @Sort(column = "create_time", sort = Sort.SortType.DESC)
    private LocalDateTime createTime;
}
