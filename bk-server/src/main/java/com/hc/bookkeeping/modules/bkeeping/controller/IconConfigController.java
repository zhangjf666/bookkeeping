package com.hc.bookkeeping.modules.bkeeping.controller;

import com.hc.bookkeeping.common.annotation.Log;
import com.hc.bookkeeping.common.model.Response;
import com.hc.bookkeeping.common.support.valid.Insert;
import com.hc.bookkeeping.common.support.valid.Update;
import com.hc.bookkeeping.common.utils.QueryUtil;
import com.hc.bookkeeping.modules.bkeeping.dto.IconConfigDto;
import com.hc.bookkeeping.modules.bkeeping.dto.IconConfigQueryDto;
import com.hc.bookkeeping.modules.bkeeping.service.IconConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/iconConfig")
@RequiredArgsConstructor
@Api(tags = "图标配置接口")
public class IconConfigController {
    private final IconConfigService iconConfigService;

    @Log("查询图标配置")
    @ApiOperation("查询图标配置")
    @GetMapping
    public Response get(@Validated IconConfigQueryDto queryDto){
        List<IconConfigDto> list = iconConfigService.queryList(QueryUtil.bulid(queryDto));
        return Response.ok(list);
    }

    @Log("创建图标配置")
    @ApiOperation("创建图标配置")
    @PostMapping
    public Response create(@Validated(Insert.class) @RequestBody IconConfigDto dto){
        iconConfigService.create(dto);
        return Response.ok();
    }

    @Log("编辑图标配置")
    @ApiOperation("编辑图标配置")
    @PutMapping
    public Response update(@Validated(Update.class) @RequestBody IconConfigDto dto){
        iconConfigService.update(dto);
        return Response.ok();
    }

    @Log("删除图标配置")
    @ApiOperation("删除图标配置")
    @DeleteMapping
    public Response delete(@RequestBody Set<Long> ids){
        iconConfigService.delete(ids);
        return Response.ok();
    }
}
