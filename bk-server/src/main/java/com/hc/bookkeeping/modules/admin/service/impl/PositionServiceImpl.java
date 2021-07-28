package com.hc.bookkeeping.modules.admin.service.impl;

import com.hc.bookkeeping.common.base.BaseServiceImpl;
import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.common.utils.QueryUtil;
import com.hc.bookkeeping.modules.admin.dto.PositionDto;
import com.hc.bookkeeping.modules.admin.dto.PositionQueryDto;
import com.hc.bookkeeping.modules.admin.entity.Position;
import com.hc.bookkeeping.modules.admin.mapper.PositionMapper;
import com.hc.bookkeeping.modules.admin.mapstruct.PositionMapstruct;
import com.hc.bookkeeping.modules.admin.service.PositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 职位表 服务实现类
 * </p>
 *
 * @author zjf
 * @since 2020-06-11
 */
@Service
@RequiredArgsConstructor
public class PositionServiceImpl extends BaseServiceImpl<PositionMapstruct, PositionDto, PositionMapper, Position> implements PositionService {

    private final PositionMapstruct positionMapstruct;

    @Override
    public Page<PositionDto> queryPage(PositionQueryDto queryDto, Page page) {
        Page rpage = Page.fromMybatisPlusPage(baseMapper.selectPage(page.toMybatisPlusPage(), QueryUtil.bulid(queryDto)));
        positionMapstruct.toDto(rpage.getRecord());
        return rpage;
    }

    @Override
    public List<PositionDto> query(PositionQueryDto queryDto) {
        return positionMapstruct.toDto(baseMapper.selectList(QueryUtil.bulid(queryDto)));
    }

    @Override
    public Set<PositionDto> getPositionByUserId(Long userId) {
        Set<Position> positions = baseMapper.getPositionByUserId(userId);
        return positions.stream().map(baseMapstruct::toDto).collect(Collectors.toSet());
    }
}
