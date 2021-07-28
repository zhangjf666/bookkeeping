package com.hc.bookkeeping.modules.admin.controller;

import com.hc.bookkeeping.common.annotation.Log;
import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.common.model.Response;
import com.hc.bookkeeping.common.support.valid.Insert;
import com.hc.bookkeeping.common.support.valid.Update;
import com.hc.bookkeeping.modules.admin.dto.PositionDto;
import com.hc.bookkeeping.modules.admin.dto.PositionQueryDto;
import com.hc.bookkeeping.modules.admin.service.PositionService;
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
@RequestMapping("/position")
@RequiredArgsConstructor
@Api(tags = "职位接口")
public class PositionController {

    private final PositionService positionService;

    @Log("查询职位")
    @ApiOperation("查询职位")
    @GetMapping
    @PreAuthorize("@ph.check('system:position:list')")
    public Response getPosition(@Validated PositionQueryDto queryDto, Page pageable){
        Page page = positionService.queryPage(queryDto, pageable);
        return Response.ok(page);
    }

    @Log("创建职位")
    @ApiOperation("创建职位")
    @PostMapping
    @PreAuthorize("@ph.check('system:position:add')")
    public Response create(@Validated(Insert.class) @RequestBody PositionDto dto){
        positionService.create(dto);
        return Response.ok();
    }

    @Log("编辑职位")
    @ApiOperation("编辑职位")
    @PutMapping
    @PreAuthorize("@ph.check('system:position:edit')")
    public Response update(@Validated(Update.class) @RequestBody PositionDto dto){
        positionService.update(dto);
        return Response.ok();
    }

    @Log("删除职位")
    @ApiOperation("删除职位")
    @DeleteMapping
    @PreAuthorize("@ph.check('system:position:del')")
    public Response delete(@RequestBody Set<Long> ids){
        positionService.removeByIds(ids);
        return Response.ok();
    }
}
