package com.hc.bookkeeping.modules.bkeeping.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.Version;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="UserTag对象", description="用户标签表")
public class UserTagDto {

    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "标签代码")
    private Long code;

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "标签名称")
    private String name;

    @ApiModelProperty(value = "标签颜色")
    private String color;

    @ApiModelProperty(value = "是否置顶(0:不置顶,1:置顶)")
    private String pinned;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;
}
