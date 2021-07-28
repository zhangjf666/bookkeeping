package com.hc.bookkeeping.modules.admin.controller;

import com.hc.bookkeeping.common.annotation.Log;
import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.common.model.Response;
import com.hc.bookkeeping.modules.admin.dto.LogQueryDto;
import com.hc.bookkeeping.modules.admin.service.LogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description: 日志接口
 * @Date: 2020/6/30 16:34
 */
@Slf4j
@RestController
@RequestMapping("/log")
@RequiredArgsConstructor
@Api(tags = "日志接口")
public class LogController {

    private final LogService logService;

    @ApiOperation("查询日志")
    @GetMapping
    @PreAuthorize("@ph.check('monitor:log:list')")
    public Response getLogs(@Validated LogQueryDto queryDto, Page pageable){
        Page page = logService.queryPage(queryDto, pageable);
        return Response.ok(page);
    }

    @ApiOperation("查询单个日志")
    @GetMapping("/{id}")
    @PreAuthorize("@ph.check('monitor:log:list')")
    public Response getLog(@PathVariable Long id){
        return Response.ok(logService.queryById(id));
    }

    @Log("删除日志")
    @ApiOperation("删除日志")
    @DeleteMapping
    @PreAuthorize("@ph.check('monitor:log:del')")
    public Response delete(@RequestBody Set<Long> ids){
        logService.removeByIds(ids);
        return Response.ok();
    }
}
