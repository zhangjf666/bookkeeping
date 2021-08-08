package com.hc.bookkeeping.modules.bkeeping.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hc.bookkeeping.common.annotation.Log;
import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.common.model.Response;
import com.hc.bookkeeping.common.support.valid.Insert;
import com.hc.bookkeeping.common.support.valid.Update;
import com.hc.bookkeeping.common.utils.QueryUtil;
import com.hc.bookkeeping.modules.bkeeping.dto.UserSearchDto;
import com.hc.bookkeeping.modules.bkeeping.dto.UserSearchQueryDto;
import com.hc.bookkeeping.modules.bkeeping.entity.UserSearch;
import com.hc.bookkeeping.modules.bkeeping.service.UserSearchService;
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
@RequestMapping("/userSearch")
@RequiredArgsConstructor
@Api(tags = "搜索记录接口")
public class UserSearchController {
    private final UserSearchService userSearchService;

    @Log("查询搜索记录")
    @ApiOperation("查询搜索记录")
    @GetMapping
    public Response get(@Validated UserSearchQueryDto queryDto){
        List<UserSearchDto> list = userSearchService.queryList(QueryUtil.bulid(queryDto));
        return Response.ok(list);
    }

    @Log("创建搜索记录")
    @ApiOperation("创建搜索记录")
    @PostMapping
    public Response create(@Validated(Insert.class) @RequestBody UserSearchDto dto){
        userSearchService.create(dto);
        return Response.ok();
    }

    @Log("编辑搜索记录")
    @ApiOperation("编辑搜索记录")
    @PutMapping
    public Response update(@Validated(Update.class) @RequestBody UserSearchDto dto){
        userSearchService.update(dto);
        return Response.ok();
    }

    @Log("删除搜索记录")
    @ApiOperation("删除搜索记录")
    @DeleteMapping
    public Response delete(@RequestBody Set<Long> ids){
        userSearchService.delete(ids);
        return Response.ok();
    }

    @Log("查询用户最近搜索记录")
    @ApiOperation("查询搜索记录")
    @GetMapping("/nearlySearch")
    public Response<List<UserSearchDto>> getNearlySearch(@RequestParam(name = "userId") Long userId,
                                                         @RequestParam(name = "size", required = false, defaultValue = "10") int size) {
        UserSearchQueryDto dto = new UserSearchQueryDto();
        dto.setUserId(userId);
        Page<UserSearchDto> page = userSearchService.queryPage(new Page(1, size), QueryUtil.bulid(dto));
        return Response.ok(page.getRecord());
    }

    @Log("删除用户搜索记录")
    @ApiOperation("删除搜索记录")
    @PostMapping("/deleteUserAllSearch")
    public Response deleteUserAllSearch(@RequestParam Long userId) {
        userSearchService.remove(Wrappers.<UserSearch>lambdaQuery().eq(UserSearch::getUserId, userId));
        return Response.ok();
    }
}
