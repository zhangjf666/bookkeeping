package com.hc.bookkeeping.modules.bkeeping.controller;

import com.hc.bookkeeping.common.annotation.Log;
import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.common.support.valid.Insert;
import com.hc.bookkeeping.common.support.valid.Update;
import com.hc.bookkeeping.common.utils.QueryUtil;
import com.hc.bookkeeping.modules.bkeeping.dto.UserTagDto;
import com.hc.bookkeeping.modules.bkeeping.dto.UserTagQueryDto;
import com.hc.bookkeeping.modules.bkeeping.service.UserTagService;
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
@RequestMapping("/userTag")
@RequiredArgsConstructor
@Api(tags = "用户标签接口")
public class UserTagController {

    private final UserTagService userTagService;

    @Log("分页查询用户标签")
    @ApiOperation("分页查询用户标签")
    @GetMapping("/page")
    public Page<UserTagDto> getPage(@Validated UserTagQueryDto queryDto, Page page) {
        return userTagService.queryPage(page, QueryUtil.bulid(queryDto));
    }

    @Log("查询用户标签")
    @ApiOperation("查询用户标签")
    @GetMapping
    public List<UserTagDto> get(@Validated UserTagQueryDto queryDto){
        return userTagService.queryList(QueryUtil.bulid(queryDto));
    }

    @Log("创建用户标签")
    @ApiOperation("创建用户标签")
    @PostMapping
    public UserTagDto create(@Validated(Insert.class) @RequestBody UserTagDto dto){
        return userTagService.create(dto);
    }

    @Log("编辑用户标签")
    @ApiOperation("编辑用户标签")
    @PutMapping
    public boolean update(@Validated(Update.class) @RequestBody UserTagDto dto){
        return userTagService.update(dto);
    }

    @Log("删除用户标签")
    @ApiOperation("删除用户标签")
    @DeleteMapping
    public boolean delete(@RequestBody Set<Long> ids){
        return userTagService.deleteByIds(ids);
    }
}
