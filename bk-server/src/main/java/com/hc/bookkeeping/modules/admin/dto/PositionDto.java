package com.hc.bookkeeping.modules.admin.dto;

import com.hc.bookkeeping.common.support.valid.Insert;
import com.hc.bookkeeping.common.support.valid.Update;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/6/8 16:13
 */
@Data
public class PositionDto {

    @ApiModelProperty(value = "id")
    @NotNull(groups = {Update.class},message = "岗位id不能为空")
    private Long id;

    @ApiModelProperty(value = "名称")
    @NotBlank(groups = {Insert.class},message = "岗位名称不能为空")
    private String name;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "英文名称")
    private String nameEn;
}
