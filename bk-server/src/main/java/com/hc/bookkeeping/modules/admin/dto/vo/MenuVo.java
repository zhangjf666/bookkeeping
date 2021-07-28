package com.hc.bookkeeping.modules.admin.dto.vo;

import com.hc.bookkeeping.common.base.SimpleLazyTree;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/8/5 11:23
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MenuVo extends SimpleLazyTree<MenuVo, Long> {
    @ApiModelProperty(value = "名称")
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

    public void setSort(Integer sort) {
        this.sort = sort;
        this.weight = sort;
    }

    private String createBy;

    private LocalDateTime createTime;

    private String updateBy;

    private LocalDateTime updateTime;
}
