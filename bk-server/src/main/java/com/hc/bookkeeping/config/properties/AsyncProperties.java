package com.hc.bookkeeping.config.properties;

import lombok.Data;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/6/24 11:05
 */
@Data
public class AsyncProperties {

    private int corePoolSize = 5;

    private int maxPoolSize = 10;

    private int keepAliveSeconds = 60;

    private int queueCapacity = 50;
}
