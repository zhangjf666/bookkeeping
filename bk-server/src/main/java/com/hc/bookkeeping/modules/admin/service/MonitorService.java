package com.hc.bookkeeping.modules.admin.service;

import com.hc.bookkeeping.modules.admin.dto.ServerInfoDto;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description: 系统监控
 * @Date: 2020/9/29 11:15
 */
public interface MonitorService {

    /**
     * 获取服务器系统信息
     * @return 服务器信息
     */
    ServerInfoDto getServerInfo();

    /**
     * 获取druid sql监控地址
     * @return druid sql监控地址
     */
    String getSqlMonitorUrl();
}
