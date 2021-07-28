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
 * @Date: 2020/7/1 9:39
 */
@Data
public class ParamDto {

    @ApiModelProperty(value = "id")
    @NotNull(groups = Update.class,message = "参数id不能为空")
    private Long id;

    @ApiModelProperty(value = "参数名")
    @NotBlank(groups = Insert.class,message = "参数名不能为空")
    private String name;

    @ApiModelProperty(value = "参数值")
    private String value;

    @ApiModelProperty(value = "参数描述")
    private String description;

}
