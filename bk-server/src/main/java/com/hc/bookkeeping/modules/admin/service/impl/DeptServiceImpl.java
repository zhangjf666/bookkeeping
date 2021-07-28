package com.hc.bookkeeping.modules.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hc.bookkeeping.common.base.BaseServiceImpl;
import com.hc.bookkeeping.common.constants.Constants;
import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.common.utils.QueryUtil;
import com.hc.bookkeeping.modules.admin.dto.DeptDto;
import com.hc.bookkeeping.modules.admin.dto.DeptQueryDto;
import com.hc.bookkeeping.modules.admin.dto.vo.DeptVo;
import com.hc.bookkeeping.modules.admin.entity.Dept;
import com.hc.bookkeeping.modules.admin.mapper.DeptMapper;
import com.hc.bookkeeping.modules.admin.mapstruct.DeptMapstruct;
import com.hc.bookkeeping.modules.admin.service.DeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 部门表 服务实现类
 * </p>
 *
 * @author zjf
 * @since 2020-06-11
 */
@Service
@RequiredArgsConstructor
public class DeptServiceImpl extends BaseServiceImpl<DeptMapstruct, DeptDto, DeptMapper, Dept> implements DeptService {

    @Override
    public Page<DeptDto> queryPage(DeptQueryDto queryDto, Page page) {
        QueryUtil.queryTreeRootCheck(queryDto);
        Page rpage = Page.fromMybatisPlusPage(baseMapper.selectPage(page.toMybatisPlusPage(), QueryUtil.bulid(queryDto)));
        rpage.setRecord(baseMapstruct.toDto(rpage.getRecord()));
        return rpage;
    }

    @Override
    public List<DeptDto> query(DeptQueryDto queryDto) {
        return baseMapstruct.toDto(baseMapper.selectList(QueryUtil.bulid(queryDto)));
    }

    @Override
    public Set<DeptDto> getDeptsByRoleId(Set<Long> roleId) {
        LinkedHashSet<Dept> depts = baseMapper.getDeptsByRoleId(roleId);
        return depts.stream().map(baseMapstruct::toDto).collect(Collectors.toSet());
    }

    @Override
    public List<DeptDto> getSuperior(DeptDto dto, List<Dept> depts) {
        if(dto.getPid().equals(Constants.TREE_ROOT)){
            depts.addAll(baseMapper.selectList(new QueryWrapper<Dept>().eq("pid", Constants.TREE_ROOT)));
            return baseMapstruct.toDto(depts);
        }
        depts.addAll(baseMapper.selectList(new QueryWrapper<Dept>().eq("pid",dto.getPid())));
        return getSuperior(queryById(dto.getPid()), depts);
    }

    @Override
    public void superiorCheckChildren(List<DeptVo> trees) {
        Queue<DeptVo> queue = new ArrayDeque<>(trees.size());
        queue.addAll(trees);

        while (!queue.isEmpty()){
            DeptVo tree = queue.remove();
            List<DeptVo> childrens = tree.getChildren();
            if(childrens == null || childrens.size() <= 0){
                tree.setHasChildren(baseMapper.selectCount(new QueryWrapper<Dept>().eq("pid",tree.getId())) > 0);
            } else {
                queue.addAll(childrens);
            }
        }
    }
}
