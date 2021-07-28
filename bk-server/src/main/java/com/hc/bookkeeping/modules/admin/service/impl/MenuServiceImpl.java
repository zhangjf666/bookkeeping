package com.hc.bookkeeping.modules.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hc.bookkeeping.common.base.BaseServiceImpl;
import com.hc.bookkeeping.common.constants.Constants;
import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.common.utils.QueryUtil;
import com.hc.bookkeeping.modules.admin.dto.MenuDto;
import com.hc.bookkeeping.modules.admin.dto.MenuQueryDto;
import com.hc.bookkeeping.modules.admin.dto.vo.MenuVo;
import com.hc.bookkeeping.modules.admin.entity.Menu;
import com.hc.bookkeeping.modules.admin.mapper.MenuMapper;
import com.hc.bookkeeping.modules.admin.mapstruct.MenuMapstruct;
import com.hc.bookkeeping.modules.admin.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 菜单表 服务实现类
 * </p>
 *
 * @author zjf
 * @since 2020-06-11
 */
@Service
@RequiredArgsConstructor
public class MenuServiceImpl extends BaseServiceImpl<MenuMapstruct, MenuDto, MenuMapper, Menu> implements MenuService {

    @Override
    public Page<MenuDto> queryPage(MenuQueryDto queryDto, Page page) {
        QueryUtil.queryTreeRootCheck(queryDto);
        Page rpage = Page.fromMybatisPlusPage(baseMapper.selectPage(page.toMybatisPlusPage(), QueryUtil.bulid(queryDto)));
        rpage.setRecord(baseMapstruct.toDto(rpage.getRecord()));
        return rpage;
    }

    @Override
    public List<MenuDto> query(MenuQueryDto queryDto) {
        return baseMapstruct.toDto(baseMapper.selectList(QueryUtil.bulid(queryDto)));
    }

    @Override
    public Set<MenuDto> getMenusByRoleId(Set<Long> roleId) {
        LinkedHashSet<Menu> menus = baseMapper.getMenusByRoleId(roleId);
        return menus.stream().map(baseMapstruct::toDto).collect(Collectors.toSet());
    }

    @Override
    public List<MenuDto> getSuperior(MenuDto dto, List<Menu> menus) {
        if(dto.getPid().equals(Constants.TREE_ROOT)){
            menus.addAll(baseMapper.selectList(new QueryWrapper<Menu>().eq("pid", Constants.TREE_ROOT)));
            return baseMapstruct.toDto(menus);
        }
        menus.addAll(baseMapper.selectList(new QueryWrapper<Menu>().eq("pid",dto.getPid())));
        return getSuperior(queryById(dto.getPid()), menus);
    }

    @Override
    public void superiorCheckChildren(List<MenuVo> trees) {
        Queue<MenuVo> queue = new ArrayDeque<>(trees.size());
        queue.addAll(trees);

        while (!queue.isEmpty()){
            MenuVo tree = queue.remove();
            List<MenuVo> childrens = tree.getChildren();
            if(childrens == null || childrens.size() <= 0){
                tree.setHasChildren(baseMapper.selectCount(new QueryWrapper<Menu>().eq("pid",tree.getId())) > 0);
            } else {
                queue.addAll(childrens);
            }
        }
    }
}
