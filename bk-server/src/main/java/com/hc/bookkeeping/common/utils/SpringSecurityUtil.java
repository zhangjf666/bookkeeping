package com.hc.bookkeeping.common.utils;

import cn.hutool.json.JSONObject;
import com.hc.bookkeeping.common.exception.BusinessException;
import com.hc.bookkeeping.common.model.ResponseCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * @Author: zhangjunfeng
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: Created on 2019/3/6 9:56
 */
public final class SpringSecurityUtil {
    /**
     * 获取当前登录的用户
     * @return UserDetails
     */
    public static UserDetails getCurrentUser() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new BusinessException(ResponseCode.UNAUTHORIZED, "当前登录状态过期");
        }
        return (UserDetails) authentication.getPrincipal();
    }

    /**
     * 获取系统用户名称
     *
     * @return 系统用户名称
     */
    public static String getCurrentUsername() {
        UserDetails userDetails = getCurrentUser();
        return userDetails.getUsername();
    }

    /**
     * 获取系统用户ID
     * @return 系统用户ID
     */
    public static Long getCurrentUserId() {
        UserDetails userDetails = getCurrentUser();
        return new JSONObject(new JSONObject(userDetails).get("user")).get("id", Long.class);
    }
}
