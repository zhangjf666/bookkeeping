package com.hc.bookkeeping.modules.security.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/6/9 14:24
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OnlineUserDto implements Serializable {
    /**
     * id
     */
    private Long id;
    /**
     * 用户名
     */
    private String userName;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 浏览器
     */
    private String browser;

    /**
     * IP
     */
    private String ip;

    /**
     * token
     */
    private String key;

    /**
     * 登录时间
     */
    private LocalDateTime loginTime;
}
