package com.hc.bookkeeping.modules.admin.dto;

import com.hc.bookkeeping.common.base.BaseDto;
import com.hc.bookkeeping.common.support.valid.Insert;
import com.hc.bookkeeping.common.support.valid.Update;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/6/9 10:15
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MenuDto extends BaseDto {
    @ApiModelProperty(value = "id")
    @NotNull(groups = Update.class,message = "菜单id不能为空")
    private Long id;

    @ApiModelProperty(value = "上级菜单id")
    private Long pid;

    @ApiModelProperty(value = "名称")
    @NotBlank(groups = Insert.class,message = "菜单名称不能为空")
    private String name;

    @ApiModelProperty(value = "菜单描述")
    private String description;

    @ApiModelProperty(value = "排序")
    private Integer sort;

    @ApiModelProperty(value = "访问url")
    private String url;

    @ApiModelProperty(value = "是否外部地址")
    private String isOutside;

    @ApiModelProperty(value = "菜单类型(1:目录,2:菜单,3:按钮)")
    private String type;

    @ApiModelProperty(value = "图标")
    private String icon;

    @ApiModelProperty(value = "组件名称")
    private String componentName;

    @ApiModelProperty(value = "组件地址")
    private String component;

    @ApiModelProperty(value = "是否显示(1:显示 0:不显示)")
    private String isShow;

    @ApiModelProperty(value = "权限标识")
    private String permission;
}
