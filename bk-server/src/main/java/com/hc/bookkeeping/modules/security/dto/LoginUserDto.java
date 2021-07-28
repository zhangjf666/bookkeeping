package com.hc.bookkeeping.modules.security.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/6/9 9:47
 */
@Getter
@Setter
public class LoginUserDto {
    @NotBlank
    private String username;

    @NotBlank
    private String password;

    private String captcha;

    private String uuid = "";

    @Override
    public String toString() {
        return "{username=" + username  + ", password= ******}";
    }
}
