package com.hc.bookkeeping.modules.admin.dto;

import com.hc.bookkeeping.common.support.valid.Insert;
import com.hc.bookkeeping.common.support.valid.Update;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/6/8 16:10
 */
@Data
public class RoleDto {
    @ApiModelProperty(value = "id")
    @NotNull(groups = {Update.class},message = "角色id不能为空")
    private Long id;

    @ApiModelProperty(value = "角色名称")
    @NotBlank(groups = Insert.class,message = "角色名称不能为空")
    private String name;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "数据范围")
    private String dataScope;

    @ApiModelProperty(value = "菜单")
    private Set<MenuDto> menus;
}
