package com.hc.bookkeeping.modules.bkeeping.controller;

import com.hc.bookkeeping.common.annotation.Anonymous;
import com.hc.bookkeeping.common.annotation.Log;
import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.common.support.valid.Insert;
import com.hc.bookkeeping.common.support.valid.Update;
import com.hc.bookkeeping.modules.bkeeping.dto.*;
import com.hc.bookkeeping.modules.bkeeping.service.IncomeExpenseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/incomeExpense")
@RequiredArgsConstructor
@Api(tags = "收入支出接口")
public class IncomeExpenseController {

    private final IncomeExpenseService incomeExpenseService;

    @Log("分页查询收入支出")
    @ApiOperation("分页查询收入支出")
    @PostMapping("/page")
    public Page<IncomeExpenseDto> getPage(@RequestBody IncomeExpenseQueryDto queryDto, Page page) {
        if (queryDto.getPageNo() != null) {
            page.setPageNo(queryDto.getPageNo());
        }
        if (queryDto.getPageSize() != null) {
            page.setPageSize(queryDto.getPageSize());
        }
        return incomeExpenseService.queryPage(page, queryDto);
    }

    @Log("查询收入支出")
    @ApiOperation("查询收入支出")
    @PostMapping("/list")
    public List<IncomeExpenseDto> get(@RequestBody IncomeExpenseQueryDto queryDto){
        return incomeExpenseService.queryList(queryDto);
    }

    @GetMapping("/test")
    @Anonymous
    public String test(HttpServletRequest request) {
        String queryString = request.getQueryString();
        String[] rawIds = request.getParameterValues("remark");

        System.out.println("原始 QueryString: " + queryString);
        System.out.println("ParameterValues 长度: " + (rawIds != null ? rawIds.length : 0));
        return "Check your console!";
    }

    @Log("创建收入支出")
    @ApiOperation("创建收入支出")
    @PostMapping
    public IncomeExpenseDto create(@Validated(Insert.class) @RequestBody IncomeExpenseDto dto){
        return incomeExpenseService.create(dto);
    }

    @Log("编辑收入支出")
    @ApiOperation("编辑收入支出")
    @PutMapping
    public boolean update(@Validated(Update.class) @RequestBody IncomeExpenseDto dto){
        return incomeExpenseService.update(dto);
    }

    @Log("删除收入支出")
    @ApiOperation("删除收入支出")
    @DeleteMapping
    public boolean delete(@RequestBody Set<Long> ids){
        return incomeExpenseService.deleteByIds(ids);
    }

    @Log("查询首页摘要信息")
    @ApiOperation("查询首页摘要信息")
    @GetMapping("/summary")
    public SummaryDto getSummary(@RequestParam(name = "userId") Long userId,
                                           @RequestParam(name = "accountBookId", required = false) Long accountBookId,
                                           @RequestParam(name = "days", required = false, defaultValue = "2") int days) {
        return incomeExpenseService.querySummary(userId, accountBookId, days);
    }

    @Log("查询账单报表信息")
    @ApiOperation("查询账单报表信息")
    @PostMapping("/sumPeriod")
    public BillResultDto getSumAmountPeriod(@RequestBody BillQueryDto billQueryDto) {
        return incomeExpenseService.querySumAmountPeriod(billQueryDto);
    }

    @Log("导出收入支出记录")
    @ApiOperation("导出收入支出记录")
    @PostMapping("/exportRecord")
    public void exportRecord(@RequestBody IncomeExpenseQueryDto queryDto, HttpServletResponse response) {
        incomeExpenseService.exportRecord(queryDto, response);
    }
}
