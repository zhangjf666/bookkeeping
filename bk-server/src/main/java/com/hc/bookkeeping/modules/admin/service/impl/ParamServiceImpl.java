package com.hc.bookkeeping.modules.admin.service.impl;

import com.hc.bookkeeping.common.base.BaseServiceImpl;
import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.common.utils.QueryUtil;
import com.hc.bookkeeping.modules.admin.dto.ParamDto;
import com.hc.bookkeeping.modules.admin.dto.ParamQueryDto;
import com.hc.bookkeeping.modules.admin.entity.Param;
import com.hc.bookkeeping.modules.admin.mapper.ParamMapper;
import com.hc.bookkeeping.modules.admin.mapstruct.ParamMapstruct;
import com.hc.bookkeeping.modules.admin.service.ParamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 系统参数表 服务实现类
 * </p>
 *
 * @author zjf
 * @since 2020-06-11
 */
@Service
@RequiredArgsConstructor
public class ParamServiceImpl extends BaseServiceImpl<ParamMapstruct, ParamDto, ParamMapper, Param> implements ParamService {

    @Override
    public Page<ParamDto> queryPage(ParamQueryDto queryDto, Page page) {
        Page rpage = Page.fromMybatisPlusPage(baseMapper.selectPage(page.toMybatisPlusPage(), QueryUtil.bulid(queryDto)));
        baseMapstruct.toDto(rpage.getRecord());
        return rpage;
    }

    @Override
    public List<ParamDto> query(ParamQueryDto queryDto) {
        return baseMapstruct.toDto(baseMapper.selectList(QueryUtil.bulid(queryDto)));
    }
}
