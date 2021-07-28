package com.hc.bookkeeping.modules.admin.controller;

import com.hc.bookkeeping.common.annotation.Log;
import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.common.model.Response;
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
    public Response getUser(@Validated UserQueryDto queryDto, Page pageable){
        Page page = userService.queryPage(queryDto, pageable);
        return Response.ok(page);
    }

    @Log("查询单个用户")
    @ApiOperation("查询单个用户")
    @GetMapping("/{id}")
    @PreAuthorize("@ph.check('system:user:list')")
    public Response getUser(@PathVariable Long id){
        return Response.ok(userService.queryById(id));
    }

    @Log("创建用户")
    @ApiOperation("创建用户")
    @PostMapping
    @PreAuthorize("@ph.check('system:user:add')")
    public Response create(@Validated(Insert.class) @RequestBody UserDto dto){
        userService.create(dto);
        return Response.ok();
    }

    @Log("编辑用户")
    @ApiOperation("编辑用户")
    @PutMapping
    @PreAuthorize("@ph.check('system:user:edit')")
    public Response update(@Validated(Update.class) @RequestBody UserDto dto){
        userService.update(dto);
        return Response.ok();
    }

    @Log("删除用户")
    @ApiOperation("删除用户")
    @DeleteMapping
    @PreAuthorize("@ph.check('system:user:del')")
    public Response delete(@RequestBody Set<Long> ids){
        for (Long id: ids) {
            userService.deleteById(id);
        }
        return Response.ok();
    }
}
