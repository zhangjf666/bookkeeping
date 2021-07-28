package com.hc.bookkeeping.modules.admin.controller;

import com.hc.bookkeeping.common.annotation.Log;
import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.common.model.Response;
import com.hc.bookkeeping.common.support.valid.Insert;
import com.hc.bookkeeping.common.support.valid.Update;
import com.hc.bookkeeping.modules.admin.dto.DictDto;
import com.hc.bookkeeping.modules.admin.dto.DictQueryDto;
import com.hc.bookkeeping.modules.admin.service.DictService;
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
 * @Date: 2020/6/30 10:50
 */
@Slf4j
@RestController
@RequestMapping("/dict")
@RequiredArgsConstructor
@Api(tags = "字典接口")
public class DictController {

    private final DictService dictService;

    @Log("查询字典")
    @ApiOperation("查询字典")
    @GetMapping
    @PreAuthorize("@ph.check('system:dict:list')")
    public Response getPosition(@Validated DictQueryDto queryDto, Page pageable){
        Page page = dictService.queryPage(queryDto, pageable);
        return Response.ok(page);
    }

    @Log("创建字典")
    @ApiOperation("创建字典")
    @PostMapping
    @PreAuthorize("@ph.check('system:dict:add')")
    public Response create(@Validated(Insert.class) @RequestBody DictDto dto){
        dictService.create(dto);
        return Response.ok();
    }

    @Log("编辑字典")
    @ApiOperation("编辑字典")
    @PutMapping
    @PreAuthorize("@ph.check('system:dict:edit')")
    public Response update(@Validated(Update.class) @RequestBody DictDto dto){
        dictService.update(dto);
        return Response.ok();
    }

    @Log("删除字典")
    @ApiOperation("删除字典")
    @DeleteMapping
    @PreAuthorize("@ph.check('system:dict:del')")
    public Response delete(@RequestBody Set<Long> ids){
        dictService.deleteDicts(ids);
        return Response.ok();
    }
}
