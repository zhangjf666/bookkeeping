package com.hc.bookkeeping.modules.admin.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hc.bookkeeping.common.annotation.Log;
import com.hc.bookkeeping.common.constants.Constants;
import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.common.model.Response;
import com.hc.bookkeeping.common.support.valid.Insert;
import com.hc.bookkeeping.common.support.valid.Update;
import com.hc.bookkeeping.common.utils.TreeUtil;
import com.hc.bookkeeping.modules.admin.dto.DictDetailDto;
import com.hc.bookkeeping.modules.admin.dto.DictDetailQueryDto;
import com.hc.bookkeeping.modules.admin.dto.vo.DictDetailVo;
import com.hc.bookkeeping.modules.admin.entity.Dict;
import com.hc.bookkeeping.modules.admin.entity.DictDetail;
import com.hc.bookkeeping.modules.admin.service.DictDetailService;
import com.hc.bookkeeping.modules.admin.service.DictService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description: 字典详情详情
 * @Date: 2020/6/30 16:34
 */
@Slf4j
@RestController
@RequestMapping("/dictDetail")
@RequiredArgsConstructor
@Api(tags = "字典详情详情接口")
public class DictDetailController {

    private final DictService dictService;

    private final DictDetailService dictDetailService;

    @Log("查询字典详情")
    @ApiOperation("查询字典详情")
    @GetMapping
    @PreAuthorize("@ph.check('system:dict:list')")
    public Response getDictDetail(@Validated DictDetailQueryDto queryDto, Page pageable) {
        String type = null;
        if(queryDto.getDictId() != null){
            Dict dict = dictService.getById(queryDto.getDictId());
            type = dict.getType();
        }
        Page page = null;
        if("1".equals(type)){
            pageable.setPageSize(9999);
        }
        page = dictDetailService.queryPage(queryDto, pageable);
        List<DictDetailDto> dtos = page.getRecord();
        List<DictDetailVo> volist =
                dtos.stream().map(dto -> BeanUtil.toBean(dto,DictDetailVo.class)).collect(Collectors.toList());
        if("1".equals(type)){
            for (DictDetailVo vo: volist) {
                vo.setHasChildren(dictDetailService.count(new QueryWrapper<DictDetail>().lambda().eq(DictDetail::getPid,
                        vo.getId())) > 0);
            }
        }
        page.setRecord(volist);
        return Response.ok(page);
    }

    @Log("字典名查询字典详情")
    @ApiOperation("字典名查询字典详情")
    @GetMapping("/map")
    @PreAuthorize("@ph.check('system:dict:list')")
    public Response getDictDetailByName(@RequestParam String dictName){
        String[] names = dictName.split("[,，]");
        Map<String, List<DictDetailDto>> dicts = MapUtil.newHashMap();
        for (String name: names) {
            Dict dict = dictService.getOne(new QueryWrapper<Dict>().eq("name", name));
            if(dict == null){
                continue;
            }
            List<DictDetailDto> dtos = dictDetailService.queryList(new QueryWrapper<DictDetail>().lambda().eq(
                    DictDetail::getDictId, dict.getId()).orderByAsc(DictDetail::getSort));
            dicts.put(name,dtos);
        }

        return Response.ok(dicts);
    }

    @Log("创建字典详情")
    @ApiOperation("创建字典详情")
    @PostMapping
    @PreAuthorize("@ph.check('system:dict:add')")
    public Response create(@Validated(Insert.class) @RequestBody DictDetailDto dto) {
        dictDetailService.create(dto);
        return Response.ok();
    }

    @Log("编辑字典详情")
    @ApiOperation("编辑字典详情")
    @PutMapping
    @PreAuthorize("@ph.check('system:dict:edit')")
    public Response update(@Validated(Update.class) @RequestBody DictDetailDto dto) {
        dictDetailService.update(dto);
        return Response.ok();
    }

    @Log("删除字典详情")
    @ApiOperation("删除字典详情")
    @DeleteMapping
    @PreAuthorize("@ph.check('system:dict:del')")
    public Response delete(@RequestBody Set<Long> ids) {
        dictDetailService.removeByIds(ids);
        return Response.ok();
    }

    @Log("查询字典详情")
    @ApiOperation("查询字典详情:根据ID获取同级与上级数据")
    @PostMapping("/superior")
    @PreAuthorize("@ph.check('system:dict:list')")
    public Response getSuperior(@RequestBody List<Long> ids) {
        Set<DictDetailDto> dictDetailDtos  = new LinkedHashSet<>();
        for (Long id : ids) {
            DictDetailDto dictDetailDto = dictDetailService.queryById(id);
            List<DictDetailDto> dictDetails = dictDetailService.getSuperior(dictDetailDto, new ArrayList<>());
            dictDetailDtos.addAll(dictDetails);
        }
        List<DictDetailVo> volist =
                dictDetailDtos.stream().map(dto -> BeanUtil.toBean(dto,DictDetailVo.class)).collect(Collectors.toList());
        volist = TreeUtil.build(volist, Constants.TREE_ROOT);
        dictDetailService.superiorCheckChildren(volist);
        return Response.ok(volist);
    }
}
