package com.hc.bookkeeping.modules.admin.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hc.bookkeeping.common.annotation.Log;
import com.hc.bookkeeping.common.constants.Constants;
import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.common.model.Response;
import com.hc.bookkeeping.common.support.valid.Insert;
import com.hc.bookkeeping.common.support.valid.Update;
import com.hc.bookkeeping.common.utils.TreeUtil;
import com.hc.bookkeeping.modules.admin.dto.DeptDto;
import com.hc.bookkeeping.modules.admin.dto.DeptQueryDto;
import com.hc.bookkeeping.modules.admin.dto.vo.DeptVo;
import com.hc.bookkeeping.modules.admin.entity.Dept;
import com.hc.bookkeeping.modules.admin.service.DeptService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/7/1 9:09
 */
@Slf4j
@RestController
@RequestMapping("/dept")
@RequiredArgsConstructor
@Api(tags = "部门接口")
public class DeptController {
    
    private final DeptService deptService;

    @Log("查询部门")
    @ApiOperation("查询部门")
    @GetMapping
    @PreAuthorize("@ph.check('system:dept:list')")
    public Response getDept(@Validated DeptQueryDto queryDto, Page pageable){
        pageable.setPageSize(9999);
        Page page = deptService.queryPage(queryDto, pageable);
        List<DeptDto> dtos = page.getRecord();
        List<DeptVo> volist =
                dtos.stream().map(dto -> BeanUtil.toBean(dto,DeptVo.class)).collect(Collectors.toList());
        for (DeptVo vo: volist) {
            vo.setHasChildren(deptService.count(new QueryWrapper<Dept>().lambda().eq(Dept::getPid,vo.getId())) > 0);
        }
        page.setRecord(volist);
        return Response.ok(page);
    }

    @Log("查询部门")
    @ApiOperation("查询部门:根据ID获取同级与上级数据")
    @PostMapping("/superior")
    @PreAuthorize("@ph.check('system:dept:list')")
    public Response getSuperior(@RequestBody List<Long> ids) {
        Set<DeptDto> deptDtos  = new LinkedHashSet<>();
        for (Long id : ids) {
            DeptDto deptDto = deptService.queryById(id);
            List<DeptDto> depts = deptService.getSuperior(deptDto, new ArrayList<>());
            deptDtos.addAll(depts);
        }
        List<DeptVo> volist =
                deptDtos.stream().map(dto -> BeanUtil.toBean(dto,DeptVo.class)).collect(Collectors.toList());
        volist = TreeUtil.build(volist, Constants.TREE_ROOT);
        deptService.superiorCheckChildren(volist);
        return Response.ok(volist);
    }

    @Log("获取所有部门树")
    @ApiOperation("获取所有部门树")
    @GetMapping("/tree")
    @PreAuthorize("@ph.check('system:menu:list')")
    public Response getDeptTree() {
        List<DeptDto> dtos = deptService.query(new DeptQueryDto());
        List<DeptVo> volist =
                dtos.stream().map(dto -> BeanUtil.toBean(dto, DeptVo.class)).collect(Collectors.toList());
        return Response.ok(TreeUtil.build(volist, Constants.TREE_ROOT));
    }

    @Log("创建部门")
    @ApiOperation("创建部门")
    @PostMapping
    @PreAuthorize("@ph.check('system:dept:add')")
    public Response create(@Validated(Insert.class) @RequestBody DeptDto dto){
        deptService.create(dto);
        return Response.ok();
    }

    @Log("编辑部门")
    @ApiOperation("编辑部门")
    @PutMapping
    @PreAuthorize("@ph.check('system:dept:edit')")
    public Response update(@Validated(Update.class) @RequestBody DeptDto dto){
        deptService.update(dto);
        return Response.ok();
    }

    @Log("删除部门")
    @ApiOperation("删除部门")
    @DeleteMapping
    @PreAuthorize("@ph.check('system:dept:del')")
    public Response delete(@RequestBody Set<Long> ids){
        deptService.removeByIds(ids);
        return Response.ok();
    }
}
