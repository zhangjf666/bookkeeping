package com.hc.bookkeeping.modules.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hc.bookkeeping.common.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 菜单表
 * </p>
 *
 * @author zjf
 * @since 2020-06-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
@ApiModel(value="Menu对象", description="菜单表")
public class Menu extends BaseEntity {

    public static final String TYPE_CATALOG = "1";
    public static final String TYPE_MENU = "2";
    public static final String TYPE_BUTTON = "3";

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "上级菜单id")
    private Long pid;

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


}
