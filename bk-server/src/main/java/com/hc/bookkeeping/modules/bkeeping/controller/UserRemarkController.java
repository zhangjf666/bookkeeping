package com.hc.bookkeeping.modules.bkeeping.controller;

import com.hc.bookkeeping.common.annotation.Log;
import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.common.support.valid.Insert;
import com.hc.bookkeeping.common.support.valid.Update;
import com.hc.bookkeeping.common.utils.QueryUtil;
import com.hc.bookkeeping.modules.bkeeping.dto.UserRemarkDto;
import com.hc.bookkeeping.modules.bkeeping.dto.UserRemarkQueryDto;
import com.hc.bookkeeping.modules.bkeeping.service.UserRemarkService;
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
@RequestMapping("/userRemark")
@RequiredArgsConstructor
@Api(tags = "用户常用备注接口")
public class UserRemarkController {

    private final UserRemarkService userRemarkService;

    @Log("分页查询用户常用备注")
    @ApiOperation("分页查询用户常用备注")
    @GetMapping("/page")
    public Page<UserRemarkDto> getPage(@Validated UserRemarkQueryDto queryDto, Page page) {
        return userRemarkService.queryPage(page, QueryUtil.bulid(queryDto));
    }

    @Log("查询用户常用备注")
    @ApiOperation("查询用户常用备注")
    @GetMapping
    public List<UserRemarkDto> get(@Validated UserRemarkQueryDto queryDto){
        return userRemarkService.queryList(QueryUtil.bulid(queryDto));
    }

    @Log("创建用户常用备注")
    @ApiOperation("创建用户常用备注")
    @PostMapping
    public UserRemarkDto create(@Validated(Insert.class) @RequestBody UserRemarkDto dto){
        return userRemarkService.create(dto);
    }

    @Log("编辑用户常用备注")
    @ApiOperation("编辑用户常用备注")
    @PutMapping
    public boolean update(@Validated(Update.class) @RequestBody UserRemarkDto dto){
        return userRemarkService.update(dto);
    }

    @Log("删除用户常用备注")
    @ApiOperation("删除用户常用备注")
    @DeleteMapping
    public boolean delete(@RequestBody Set<Long> ids){
        return userRemarkService.deleteByIds(ids);
    }
}
