package com.hc.bookkeeping.modules.bkeeping.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ChangePasswordDto {

    @ApiModelProperty(value = "当前密码")
    @NotBlank(message = "当前密码不能为空")
    private String oldPassword;

    @ApiModelProperty(value = "新密码")
    @NotBlank(message = "新密码不能为空")
    private String newPassword;

    @ApiModelProperty(value = "确认新密码")
    @NotBlank(message = "确认新密码不能为空")
    private String confirmNewPassword;
}
