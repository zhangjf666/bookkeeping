package com.hc.bookkeeping.modules.bkeeping.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="UserSearch对象", description="用户搜索记录表")
public class UserSearchDto {

    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "搜索内容")
    private String content;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;
}
