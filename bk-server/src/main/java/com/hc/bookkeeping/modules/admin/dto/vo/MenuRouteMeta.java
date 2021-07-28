package com.hc.bookkeeping.modules.admin.dto.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/8/5 11:28
 */
@Data
@AllArgsConstructor
public class MenuRouteMeta {

    private String title;

    private String icon;

    private Boolean noCache;
}
