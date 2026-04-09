package com.hc.bookkeeping.modules.bkeeping.controller;

import com.hc.bookkeeping.common.annotation.Log;
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
    public List<IconConfigDto> get(@Validated IconConfigQueryDto queryDto){
        return iconConfigService.queryList(QueryUtil.bulid(queryDto));
    }

    @Log("创建图标配置")
    @ApiOperation("创建图标配置")
    @PostMapping
    public IconConfigDto create(@Validated(Insert.class) @RequestBody IconConfigDto dto){
        return iconConfigService.create(dto);
    }

    @Log("编辑图标配置")
    @ApiOperation("编辑图标配置")
    @PutMapping
    public boolean update(@Validated(Update.class) @RequestBody IconConfigDto dto){
        return iconConfigService.update(dto);
    }

    @Log("删除图标配置")
    @ApiOperation("删除图标配置")
    @DeleteMapping
    public boolean delete(@RequestBody Set<Long> ids){
        return iconConfigService.deleteByIds(ids);
    }
}
