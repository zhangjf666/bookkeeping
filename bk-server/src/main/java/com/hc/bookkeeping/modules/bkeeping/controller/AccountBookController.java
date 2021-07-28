package com.hc.bookkeeping.modules.bkeeping.controller;

import com.hc.bookkeeping.common.annotation.Log;
import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.common.model.Response;
import com.hc.bookkeeping.common.support.valid.Insert;
import com.hc.bookkeeping.common.support.valid.Update;
import com.hc.bookkeeping.modules.admin.dto.DictQueryDto;
import com.hc.bookkeeping.modules.bkeeping.dto.AccountBookDto;
import com.hc.bookkeeping.modules.bkeeping.dto.AccountBookQueryDto;
import com.hc.bookkeeping.modules.bkeeping.service.AccountBookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/accountBook")
@RequiredArgsConstructor
@Api(tags = "账本接口")
public class AccountBookController {

    private final AccountBookService accountBookService;

    @Log("查询账本")
    @ApiOperation("查询账本")
    @GetMapping
    public Response getPosition(@Validated AccountBookQueryDto queryDto, Page pageable){
        Page page = accountBookService.queryPage(queryDto, pageable);
        return Response.ok(page);
    }

    @Log("创建账本")
    @ApiOperation("创建账本")
    @PostMapping
    public Response create(@Validated(Insert.class) @RequestBody AccountBookDto dto){
        accountBookService.create(dto);
        return Response.ok();
    }

    @Log("编辑账本")
    @ApiOperation("编辑账本")
    @PutMapping
    public Response update(@Validated(Update.class) @RequestBody AccountBookDto dto){
        accountBookService.update(dto);
        return Response.ok();
    }

    @Log("删除账本")
    @ApiOperation("删除账本")
    @DeleteMapping
    public Response delete(@RequestBody Set<Long> ids){
        accountBookService.delete(ids);
        return Response.ok();
    }
}
