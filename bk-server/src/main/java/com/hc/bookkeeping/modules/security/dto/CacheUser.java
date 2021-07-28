package com.hc.bookkeeping.modules.security.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hc.bookkeeping.modules.admin.dto.DeptDto;
import com.hc.bookkeeping.modules.admin.dto.PositionDto;
import com.hc.bookkeeping.modules.admin.dto.RoleDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/6/22 10:00
 */
@Data
public class CacheUser implements Serializable {
    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "密码")
    @JsonIgnore
    private String password;

    @ApiModelProperty(value = "昵称")
    private String nickName;

    @ApiModelProperty(value = "性别")
    private String gender;

    @ApiModelProperty(value = "邮箱")
    private String email;

    @ApiModelProperty(value = "电话")
    private String phone;

    @ApiModelProperty(value = "手机")
    private String mobilePhone;

    @ApiModelProperty(value = "用户类型(0:普通用户,1:超级管理员)")
    private String type;

    @ApiModelProperty(value = "用户头像")
    private String avatar;

    @ApiModelProperty(value = "状态:1:启用 0:禁用")
    private Boolean enabled;

    @ApiModelProperty(value = "备注信息")
    private String remarks;

    @ApiModelProperty(value = "删除标记(0:未删除,1已删除)")
    private String delFlag;

    @ApiModelProperty(value = "角色")
    private Set<RoleDto> roles;

    private Collection<GrantedAuthority> authorities;
}
