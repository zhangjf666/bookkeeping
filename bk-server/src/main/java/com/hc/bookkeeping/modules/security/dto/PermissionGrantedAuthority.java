package com.hc.bookkeeping.modules.security.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/6/22 16:22
 */
@Data
@AllArgsConstructor
public class PermissionGrantedAuthority implements GrantedAuthority {

    public PermissionGrantedAuthority() { }

    private String permission;

    @Override
    @JsonIgnore
    public String getAuthority() {
        return permission;
    }

    @Override
    public String toString(){
        return this.permission;
    }
}
