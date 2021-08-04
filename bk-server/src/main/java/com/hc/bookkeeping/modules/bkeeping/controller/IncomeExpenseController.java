package com.hc.bookkeeping.modules.bkeeping.controller;

import com.hc.bookkeeping.common.annotation.Log;
import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.common.model.Response;
import com.hc.bookkeeping.common.support.valid.Insert;
import com.hc.bookkeeping.common.support.valid.Update;
import com.hc.bookkeeping.modules.bkeeping.dto.IncomeExpenseDto;
import com.hc.bookkeeping.modules.bkeeping.dto.IncomeExpenseQueryDto;
import com.hc.bookkeeping.modules.bkeeping.dto.SummaryDto;
import com.hc.bookkeeping.modules.bkeeping.service.IncomeExpenseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/incomeExpense")
@RequiredArgsConstructor
@Api(tags = "收入支出接口")
public class IncomeExpenseController {

    private final IncomeExpenseService incomeExpenseService;

    @Log("查询收入支出")
    @ApiOperation("查询收入支出")
    @GetMapping
    public Response getPosition(@Validated IncomeExpenseQueryDto queryDto, Page pageable){
        Page page = incomeExpenseService.queryPage(queryDto, pageable);
        return Response.ok(page);
    }

    @Log("创建收入支出")
    @ApiOperation("创建收入支出")
    @PostMapping
    public Response create(@Validated(Insert.class) @RequestBody IncomeExpenseDto dto){
        incomeExpenseService.create(dto);
        return Response.ok();
    }

    @Log("编辑收入支出")
    @ApiOperation("编辑收入支出")
    @PutMapping
    public Response update(@Validated(Update.class) @RequestBody IncomeExpenseDto dto){
        incomeExpenseService.update(dto);
        return Response.ok();
    }

    @Log("删除收入支出")
    @ApiOperation("删除收入支出")
    @DeleteMapping
    public Response delete(@RequestBody Set<Long> ids){
        incomeExpenseService.delete(ids);
        return Response.ok();
    }

    @Log("查询首页摘要信息")
    @ApiOperation("查询首页摘要信息")
    @GetMapping("/summary")
    public Response<SummaryDto> getSummary(@RequestParam(name = "userId") Long userId,
                                           @RequestParam(name = "days", required = false, defaultValue = "3") int days) {
        return Response.ok(incomeExpenseService.querySummary(userId, days));
    }
}
