package com.hc.bookkeeping.common.model;

import lombok.Getter;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/6/8 13:54
 */
@Getter
public enum LogType {
    /**
     * 用户相关
     */
    USER("0"),
    /**
     * 操作
     */
    OPERATION("1"),
    /**
     * 计划任务
     */
    SCHEDULED("2");

    LogType(String type){
        this.type = type;
    }

    private String type;
}
