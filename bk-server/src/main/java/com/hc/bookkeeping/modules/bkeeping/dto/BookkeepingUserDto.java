package com.hc.bookkeeping.modules.bkeeping.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hc.bookkeeping.common.support.valid.Insert;
import com.hc.bookkeeping.common.support.valid.Update;
import com.hc.bookkeeping.modules.admin.dto.RoleDto;
import com.hc.bookkeeping.modules.bkeeping.model.GenderEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Data
public class BookkeepingUserDto {

    @ApiModelProperty(value = "id")
    @NotNull(groups = {Update.class},message = "用户id不能为空")
    private Long id;

    @ApiModelProperty(value = "用户名")
    @NotBlank(groups = Insert.class,message = "用户名不能为空")
    private String username;

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

    @ApiModelProperty(value = "用户头像")
    private String avatar;

}
