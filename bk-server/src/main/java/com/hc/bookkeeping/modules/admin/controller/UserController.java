package com.hc.bookkeeping.modules.admin.controller;

import com.hc.bookkeeping.common.annotation.Log;
import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.common.support.valid.Insert;
import com.hc.bookkeeping.common.support.valid.Update;
import com.hc.bookkeeping.modules.admin.dto.UserDto;
import com.hc.bookkeeping.modules.admin.dto.UserQueryDto;
import com.hc.bookkeeping.modules.admin.service.UserService;
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
 * @Description:
 * @Date: 2020/7/1 9:09
 */
@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Api(tags = "用户接口")
public class UserController {
    
    private final UserService userService;

    @Log("查询用户")
    @ApiOperation("查询用户")
    @GetMapping
    @PreAuthorize("@ph.check('system:user:list')")
    public Page getUser(@Validated UserQueryDto queryDto, Page pageable){
        return userService.queryPage(queryDto, pageable);
    }

    @Log("查询单个用户")
    @ApiOperation("查询单个用户")
    @GetMapping("/{id}")
    @PreAuthorize("@ph.check('system:user:list')")
    public UserDto getUser(@PathVariable Long id){
        return userService.queryById(id);
    }

    @Log("创建用户")
    @ApiOperation("创建用户")
    @PostMapping
    @PreAuthorize("@ph.check('system:user:add')")
    public UserDto create(@Validated(Insert.class) @RequestBody UserDto dto){
        return userService.create(dto);
    }

    @Log("编辑用户")
    @ApiOperation("编辑用户")
    @PutMapping
    @PreAuthorize("@ph.check('system:user:edit')")
    public boolean update(@Validated(Update.class) @RequestBody UserDto dto){
        return userService.update(dto);
    }

    @Log("删除用户")
    @ApiOperation("删除用户")
    @DeleteMapping
    @PreAuthorize("@ph.check('system:user:del')")
    public boolean delete(@RequestBody Set<Long> ids){
        return userService.deleteByIds(ids);
    }
}
