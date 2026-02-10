package com.hc.bookkeeping.modules.bkeeping.controller;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.hc.bookkeeping.common.annotation.Log;
import com.hc.bookkeeping.common.exception.BusinessException;
import com.hc.bookkeeping.common.model.Response;
import com.hc.bookkeeping.common.model.ResponseCode;
import com.hc.bookkeeping.common.support.valid.Insert;
import com.hc.bookkeeping.common.support.valid.Update;
import com.hc.bookkeeping.common.utils.QueryUtil;
import com.hc.bookkeeping.modules.bkeeping.dto.AdditionalExpenseLimitDto;
import com.hc.bookkeeping.modules.bkeeping.dto.UserConfigDto;
import com.hc.bookkeeping.modules.bkeeping.dto.UserConfigQueryDto;
import com.hc.bookkeeping.modules.bkeeping.service.UserConfigService;
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
@RequestMapping("/userConfig")
@RequiredArgsConstructor
@Api(tags = "用户配置接口")
public class UserConfigController {

    private final UserConfigService userConfigService;

    @Log("查询用户配置")
    @ApiOperation("查询用户配置")
    @GetMapping
    public Response get(@Validated UserConfigQueryDto queryDto){
        List<UserConfigDto> list = userConfigService.queryList(QueryUtil.bulid(queryDto));
        return Response.ok(list);
    }

    @Log("创建用户配置")
    @ApiOperation("创建用户配置")
    @PostMapping
    public Response create(@Validated(Insert.class) @RequestBody UserConfigDto dto){
        userConfigService.create(dto);
        return Response.ok();
    }

    @Log("编辑用户配置")
    @ApiOperation("编辑用户配置")
    @PutMapping
    public Response update(@Validated(Update.class) @RequestBody UserConfigDto dto){
        userConfigService.update(dto);
        return Response.ok();
    }

    @Log("删除用户配置")
    @ApiOperation("删除用户配置")
    @DeleteMapping
    public Response delete(@RequestBody Set<Long> ids){
        userConfigService.delete(ids);
        return Response.ok();
    }

    @Log("设置额外的支出限额")
    @ApiOperation("设置额外的支出限额")
    @PostMapping(value = "/additionalExpenseLimit")
    public Response setAdditionalExpenseLimit(@RequestBody AdditionalExpenseLimitDto dto){
        if(!StrUtil.isNumeric(dto.getExpenseLimit())){
            throw new BusinessException(ResponseCode.PARAM_ERROR, "支出限额格式错误");
        }
        userConfigService.setAdditionalExpenseLimit(dto.getUserId(), dto.getType(), dto.getExpenseLimit());
        return Response.ok();
    }
}
