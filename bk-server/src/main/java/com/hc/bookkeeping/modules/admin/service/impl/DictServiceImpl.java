package com.hc.bookkeeping.modules.admin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hc.bookkeeping.common.base.BaseServiceImpl;
import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.common.utils.QueryUtil;
import com.hc.bookkeeping.modules.admin.dto.DictDto;
import com.hc.bookkeeping.modules.admin.dto.DictQueryDto;
import com.hc.bookkeeping.modules.admin.entity.Dict;
import com.hc.bookkeeping.modules.admin.entity.DictDetail;
import com.hc.bookkeeping.modules.admin.mapper.DictDetailMapper;
import com.hc.bookkeeping.modules.admin.mapper.DictMapper;
import com.hc.bookkeeping.modules.admin.mapstruct.DictMapstruct;
import com.hc.bookkeeping.modules.admin.service.DictService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * <p>
 * 数据字典表 服务实现类
 * </p>
 *
 * @author zjf
 * @since 2020-06-11
 */
@Service
@RequiredArgsConstructor
public class DictServiceImpl extends BaseServiceImpl<DictMapstruct, DictDto, DictMapper, Dict> implements DictService {

    private final DictDetailMapper dictDetailMapper;

    @Override
    public Page<DictDto> queryPage(DictQueryDto queryDto, Page page) {
        Page rpage = Page.fromMybatisPlusPage(baseMapper.selectPage(page.toMybatisPlusPage(), QueryUtil.bulid(queryDto)));
        rpage.setRecord(baseMapstruct.toDto(rpage.getRecord()));
        return rpage;
    }

    @Override
    public List<DictDto> query(DictQueryDto queryDto) {
        return baseMapstruct.toDto(baseMapper.selectList(QueryUtil.bulid(queryDto)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDicts(Set<Long> ids) {
        for (Long id: ids) {
            baseMapper.deleteById(id);
            dictDetailMapper.delete(Wrappers.<DictDetail>lambdaQuery().eq(DictDetail::getDictId,id));
        }
        return false;
    }
}
