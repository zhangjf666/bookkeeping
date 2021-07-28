package com.hc.bookkeeping.modules.security.config;

import lombok.Data;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/6/8 15:23
 */
@Data
public class JwtProperties {
    /** Request Headers ： Authorization */
    private String header;

    /** 令牌前缀，最后留个空格 Bearer */
    private String tokenStartWith;

    /** 必须使用最少88位的Base64对该令牌进行编码 */
    private String base64Secret;

    /** 令牌过期时间,单位:秒 */
    private Long tokenValidity;

    /** token 续期检查,单位:秒 */
    private Long detect;

    /** 续期时间,单位:秒 */
    private Long renew;
}
