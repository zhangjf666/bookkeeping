package com.hc.bookkeeping.modules.bkeeping.controller;

import cn.hutool.core.lang.Validator;
import com.hc.bookkeeping.common.annotation.Log;
import com.hc.bookkeeping.common.exception.BusinessException;
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
    public List<UserConfigDto> get(@Validated UserConfigQueryDto queryDto){
        return userConfigService.queryList(QueryUtil.bulid(queryDto));
    }

    @Log("创建用户配置")
    @ApiOperation("创建用户配置")
    @PostMapping
    public UserConfigDto create(@Validated(Insert.class) @RequestBody UserConfigDto dto){
        return userConfigService.create(dto);
    }

    @Log("编辑用户配置")
    @ApiOperation("编辑用户配置")
    @PutMapping
    public boolean update(@Validated(Update.class) @RequestBody UserConfigDto dto){
        return userConfigService.update(dto);
    }

    @Log("删除用户配置")
    @ApiOperation("删除用户配置")
    @DeleteMapping
    public boolean delete(@RequestBody Set<Long> ids){
        return userConfigService.deleteByIds(ids);
    }

    @Log("设置额外的支出限额")
    @ApiOperation("设置额外的支出限额")
    @PostMapping(value = "/additionalExpenseLimit")
    public boolean setAdditionalExpenseLimit(@RequestBody AdditionalExpenseLimitDto dto){
        if(!Validator.isMoney(dto.getExpenseLimit())){
            throw new BusinessException(ResponseCode.PARAM_ERROR, "支出限额格式错误");
        }
        return userConfigService.setAdditionalExpenseLimit(dto.getUserId(), dto.getType(), dto.getExpenseLimit());
    }
}
