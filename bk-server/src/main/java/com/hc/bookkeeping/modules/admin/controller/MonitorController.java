package com.hc.bookkeeping.modules.admin.controller;

import com.hc.bookkeeping.common.annotation.Log;
import com.hc.bookkeeping.common.model.Response;
import com.hc.bookkeeping.modules.admin.service.MonitorService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/9/29 10:52
 */
@Slf4j
@RestController
@RequestMapping("/monitor")
@RequiredArgsConstructor
@Api(tags = "系统监控信息接口")
public class MonitorController {

    private final MonitorService monitorService;

    @Log("获取服务器信息")
    @ApiOperation("获取服务器信息")
    @GetMapping("/server")
    @PreAuthorize("@ph.check('monitor:server:list')")
    public Response getServerInfo(){
        return Response.ok(monitorService.getServerInfo());
    }

    @Log("获取sql监控地址")
    @ApiOperation("获取sql监控地址")
    @GetMapping("/sqlApi")
    @PreAuthorize("@ph.check('monitor:sql:list')")
    public Response getSqlMonitorUrl(){
        return Response.ok(monitorService.getSqlMonitorUrl());
    }
}
