package com.hc.bookkeeping.modules.security.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2021/6/4 9:03
 */
@Data
public class RegisterUserDto {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    @NotBlank
    private String repeatPassword;
}
