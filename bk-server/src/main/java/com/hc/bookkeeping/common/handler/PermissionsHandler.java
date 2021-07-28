package com.hc.bookkeeping.common.handler;

import com.hc.bookkeeping.common.utils.SpringSecurityUtil;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.hc.bookkeeping.common.constants.Constants.ADMIN_PERMISSIONS_KEY;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/6/17 9:25
 */
@Service(value = "ph")
public class PermissionsHandler {
    public Boolean check(String...permissions){
        // 获取当前用户的所有权限
        List<String> userPermissions = SpringSecurityUtil.getCurrentUser().getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
        // 判断当前用户的所有权限是否包含接口上定义的权限
        return userPermissions.contains(ADMIN_PERMISSIONS_KEY) || Arrays.stream(permissions).anyMatch(userPermissions::contains);
    }
}
