package com.hc.bookkeeping.modules.bkeeping.controller;

import com.hc.bookkeeping.common.annotation.Log;
import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.common.support.valid.Insert;
import com.hc.bookkeeping.common.support.valid.Update;
import com.hc.bookkeeping.common.utils.QueryUtil;
import com.hc.bookkeeping.modules.bkeeping.dto.AccountBookDto;
import com.hc.bookkeeping.modules.bkeeping.dto.AccountBookQueryDto;
import com.hc.bookkeeping.modules.bkeeping.service.AccountBookService;
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
@RequestMapping("/accountBook")
@RequiredArgsConstructor
@Api(tags = "账本接口")
public class AccountBookController {

    private final AccountBookService accountBookService;

    @Log("分页查询账本")
    @ApiOperation("分页查询账本")
    @GetMapping("/page")
    public Page<AccountBookDto> getPage(@Validated AccountBookQueryDto queryDto, Page page) {
        return accountBookService.queryPage(page, QueryUtil.bulid(queryDto));
    }

    @Log("查询账本")
    @ApiOperation("查询账本")
    @GetMapping
    public List<AccountBookDto> get(@Validated AccountBookQueryDto queryDto){
        return accountBookService.queryList(QueryUtil.bulid(queryDto));
    }

    @Log("创建账本")
    @ApiOperation("创建账本")
    @PostMapping
    public AccountBookDto create(@Validated(Insert.class) @RequestBody AccountBookDto dto){
        return accountBookService.create(dto);
    }

    @Log("编辑账本")
    @ApiOperation("编辑账本")
    @PutMapping
    public boolean update(@Validated(Update.class) @RequestBody AccountBookDto dto){
        return accountBookService.update(dto);
    }

    @Log("删除账本")
    @ApiOperation("删除账本")
    @DeleteMapping
    public boolean delete(@RequestBody Set<Long> ids){
        return accountBookService.deleteByIds(ids);
    }
}
