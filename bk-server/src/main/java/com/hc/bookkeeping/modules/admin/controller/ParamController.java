package com.hc.bookkeeping.modules.admin.controller;

import com.hc.bookkeeping.common.annotation.Log;
import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.common.model.Response;
import com.hc.bookkeeping.common.support.valid.Insert;
import com.hc.bookkeeping.common.support.valid.Update;
import com.hc.bookkeeping.modules.admin.dto.ParamDto;
import com.hc.bookkeeping.modules.admin.dto.ParamQueryDto;
import com.hc.bookkeeping.modules.admin.service.ParamService;
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
 * @Date: 2020/6/29 8:35
 */
@Slf4j
@RestController
@RequestMapping("/param")
@RequiredArgsConstructor
@Api(tags = "系统参数接口")
public class ParamController {

    private final ParamService paramService;

    @Log("查询系统参数")
    @ApiOperation("查询系统参数")
    @GetMapping
    @PreAuthorize("@ph.check('system:param:list')")
    public Response getParam(@Validated ParamQueryDto queryDto, Page pageable){
        Page page = paramService.queryPage(queryDto, pageable);
        return Response.ok(page);
    }

    @Log("创建系统参数")
    @ApiOperation("创建系统参数")
    @PostMapping
    @PreAuthorize("@ph.check('system:param:add')")
    public Response create(@Validated(Insert.class) @RequestBody ParamDto dto){
        paramService.create(dto);
        return Response.ok();
    }

    @Log("编辑系统参数")
    @ApiOperation("编辑系统参数")
    @PutMapping
    @PreAuthorize("@ph.check('system:param:edit')")
    public Response update(@Validated(Update.class) @RequestBody ParamDto dto){
        paramService.update(dto);
        return Response.ok();
    }

    @Log("删除系统参数")
    @ApiOperation("删除系统参数")
    @DeleteMapping
    @PreAuthorize("@ph.check('system:param:del')")
    public Response delete(@RequestBody Set<Long> ids){
        paramService.removeByIds(ids);
        return Response.ok();
    }
}
