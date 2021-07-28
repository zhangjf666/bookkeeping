package com.hc.bookkeeping.modules.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hc.bookkeeping.common.base.BaseServiceImpl;
import com.hc.bookkeeping.common.constants.Constants;
import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.common.utils.QueryUtil;
import com.hc.bookkeeping.modules.admin.dto.DictDetailDto;
import com.hc.bookkeeping.modules.admin.dto.DictDetailQueryDto;
import com.hc.bookkeeping.modules.admin.dto.vo.DictDetailVo;
import com.hc.bookkeeping.modules.admin.entity.DictDetail;
import com.hc.bookkeeping.modules.admin.mapper.DictDetailMapper;
import com.hc.bookkeeping.modules.admin.mapstruct.DictDetailMapstruct;
import com.hc.bookkeeping.modules.admin.service.DictDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

/**
 * <p>
 * 字典明细表 服务实现类
 * </p>
 *
 * @author zjf
 * @since 2020-06-11
 */
@Service
@RequiredArgsConstructor
public class DictDetailServiceImpl extends BaseServiceImpl<DictDetailMapstruct, DictDetailDto, DictDetailMapper, DictDetail>
        implements DictDetailService {

    @Override
    public Page<DictDetailDto> queryPage(DictDetailQueryDto queryDto, Page page) {
        QueryUtil.queryTreeRootCheck(queryDto);
        Page rpage = Page.fromMybatisPlusPage(baseMapper.selectPage(page.toMybatisPlusPage(), QueryUtil.bulid(queryDto)));
        rpage.setRecord(baseMapstruct.toDto(rpage.getRecord()));
        return rpage;
    }

    @Override
    public List<DictDetailDto> query(DictDetailQueryDto queryDto) {
        return baseMapstruct.toDto(baseMapper.selectList(QueryUtil.bulid(queryDto)));
    }

    @Override
    public List<DictDetailDto> getSuperior(DictDetailDto dto, List<DictDetail> dictDetails) {
        if(dto.getPid().equals(Constants.TREE_ROOT)){
            dictDetails.addAll(baseMapper.selectList(new QueryWrapper<DictDetail>().eq("pid", Constants.TREE_ROOT).eq(
                    "dict_id",dto.getDictId())));
            return baseMapstruct.toDto(dictDetails);
        }
        dictDetails.addAll(baseMapper.selectList(new QueryWrapper<DictDetail>().eq("pid",dto.getPid()).eq("dict_id",
                dto.getDictId())));
        return getSuperior(queryById(dto.getPid()), dictDetails);
    }

    @Override
    public void superiorCheckChildren(List<DictDetailVo> trees) {
        Queue<DictDetailVo> queue = new ArrayDeque<>(trees.size());
        queue.addAll(trees);

        while (!queue.isEmpty()){
            DictDetailVo tree = queue.remove();
            List<DictDetailVo> childrens = tree.getChildren();
            if(childrens == null || childrens.size() <= 0){
                tree.setHasChildren(baseMapper.selectCount(new QueryWrapper<DictDetail>().eq("pid",tree.getId())) > 0);
            } else {
                queue.addAll(childrens);
            }
        }
    }
}
