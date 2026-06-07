package com.hc.bookkeeping.modules.bkeeping.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.hc.bookkeeping.common.base.BaseEntity;
import com.hc.bookkeeping.common.model.BoolEnum;
import com.hc.bookkeeping.modules.admin.entity.Role;
import com.hc.bookkeeping.modules.bkeeping.model.GenderEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
@ApiModel(value="BookkeepingUser对象", description="用户表")
public class BookkeepingUser extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "密码")
    private String password;

    @ApiModelProperty(value = "昵称")
    private String nickName;

    @ApiModelProperty(value = "性别")
    private GenderEnum gender;

    @ApiModelProperty(value = "邮箱")
    private String email;

    @ApiModelProperty(value = "电话")
    private String phone;

    @ApiModelProperty(value = "手机")
    private String mobilePhone;

    @ApiModelProperty(value = "用户类型(0:普通用户,1:超级管理员))")
    private String type;

    @ApiModelProperty(value = "用户头像")
    private String avatar;

    @ApiModelProperty(value = "状态:1:启用 0:禁用")
    private BoolEnum enabled;

    @ApiModelProperty(value = "备注信息")
    private String remarks;

    @ApiModelProperty(value = "删除标记(0:未删除,1已删除)")
    @TableLogic
    private BoolEnum delFlag;

    @ApiModelProperty(value = "角色")
    @TableField(exist = false)
    private Set<Role> roles;
}
