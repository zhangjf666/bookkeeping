package com.hc.bookkeeping.modules.admin.controller;

import com.hc.bookkeeping.common.annotation.Log;
import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.common.model.Response;
import com.hc.bookkeeping.common.support.valid.Insert;
import com.hc.bookkeeping.common.support.valid.Update;
import com.hc.bookkeeping.modules.admin.dto.RoleDto;
import com.hc.bookkeeping.modules.admin.dto.RoleQueryDto;
import com.hc.bookkeeping.modules.admin.service.RoleService;
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
@RequestMapping("/role")
@RequiredArgsConstructor
@Api(tags = "角色接口")
public class RoleController {
    
    private final RoleService roleService;

    @Log("查询角色")
    @ApiOperation("查询角色")
    @GetMapping
    @PreAuthorize("@ph.check('system:role:list')")
    public Response getRole(@Validated RoleQueryDto queryDto, Page pageable){
        Page page = roleService.queryPage(queryDto, pageable);
        return Response.ok(page);
    }

    @Log("查询所有角色")
    @ApiOperation("查询所有角色")
    @GetMapping("/all")
    @PreAuthorize("@ph.check('system:role:list')")
    public Response getAllRole(){
        return Response.ok(roleService.query(new RoleQueryDto()));
    }

    @Log("查询单个角色")
    @ApiOperation("查询单个角色")
    @GetMapping("/{id}")
    @PreAuthorize("@ph.check('system:role:list')")
    public Response getRole(@PathVariable Long id){
        return Response.ok(roleService.queryById(id));
    }

    @Log("创建角色")
    @ApiOperation("创建角色")
    @PostMapping
    @PreAuthorize("@ph.check('system:role:add')")
    public Response create(@Validated(Insert.class) @RequestBody RoleDto dto){
        roleService.create(dto);
        return Response.ok();
    }

    @Log("编辑角色")
    @ApiOperation("编辑角色")
    @PutMapping
    @PreAuthorize("@ph.check('system:role:edit')")
    public Response update(@Validated(Update.class) @RequestBody RoleDto dto){
        roleService.update(dto);
        return Response.ok();
    }

    @Log("删除角色")
    @ApiOperation("删除角色")
    @DeleteMapping
    @PreAuthorize("@ph.check('system:role:del')")
    public Response delete(@RequestBody Set<Long> ids){
        for (Long id: ids) {
            roleService.deleteById(id);
        }
        return Response.ok();
    }
}
