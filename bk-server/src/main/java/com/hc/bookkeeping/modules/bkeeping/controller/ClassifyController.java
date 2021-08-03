package com.hc.bookkeeping.modules.bkeeping.controller;

import com.hc.bookkeeping.common.annotation.Log;
import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.common.model.Response;
import com.hc.bookkeeping.common.support.valid.Insert;
import com.hc.bookkeeping.common.support.valid.Update;
import com.hc.bookkeeping.common.utils.QueryUtil;
import com.hc.bookkeeping.modules.bkeeping.dto.ClassifyDto;
import com.hc.bookkeeping.modules.bkeeping.dto.ClassifyQueryDto;
import com.hc.bookkeeping.modules.bkeeping.service.ClassifyService;
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
@RequestMapping("/classify")
@RequiredArgsConstructor
@Api(tags = "分类接口")
public class ClassifyController {

    private final ClassifyService classifyService;

    @Log("查询分类")
    @ApiOperation("查询分类")
    @GetMapping
    public Response getPosition(@Validated ClassifyQueryDto queryDto){
        List<ClassifyDto> list = classifyService.queryList(QueryUtil.bulid(queryDto));
        return Response.ok(list);
    }

    @Log("创建分类")
    @ApiOperation("创建分类")
    @PostMapping
    public Response create(@Validated(Insert.class) @RequestBody ClassifyDto dto){
        classifyService.create(dto);
        return Response.ok();
    }

    @Log("编辑分类")
    @ApiOperation("编辑分类")
    @PutMapping
    public Response update(@Validated(Update.class) @RequestBody ClassifyDto dto){
        classifyService.update(dto);
        return Response.ok();
    }

    @Log("删除分类")
    @ApiOperation("删除分类")
    @DeleteMapping
    public Response delete(@RequestBody Set<Long> ids){
        classifyService.delete(ids);
        return Response.ok();
    }
}
