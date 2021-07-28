package com.hc.bookkeeping.modules.admin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hc.bookkeeping.common.base.BaseServiceImpl;
import com.hc.bookkeeping.common.constants.Constants;
import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.common.utils.QueryUtil;
import com.hc.bookkeeping.modules.admin.dto.*;
import com.hc.bookkeeping.modules.admin.entity.Role;
import com.hc.bookkeeping.modules.admin.entity.RoleDept;
import com.hc.bookkeeping.modules.admin.entity.RoleMenu;
import com.hc.bookkeeping.modules.admin.mapper.RoleDeptMapper;
import com.hc.bookkeeping.modules.admin.mapper.RoleMapper;
import com.hc.bookkeeping.modules.admin.mapper.RoleMenuMapper;
import com.hc.bookkeeping.modules.admin.mapstruct.RoleMapstruct;
import com.hc.bookkeeping.modules.admin.service.DeptService;
import com.hc.bookkeeping.modules.admin.service.MenuService;
import com.hc.bookkeeping.modules.admin.service.RoleService;
import com.hc.bookkeeping.modules.security.dto.PermissionGrantedAuthority;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.hc.bookkeeping.constants.SystemConstants.ROLE_DATASCOPE_CUSTOM;


/**
 * <p>
 * 角色表 服务实现类
 * </p>
 *
 * @author zjf
 * @since 2020-06-11
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends BaseServiceImpl<RoleMapstruct, RoleDto, RoleMapper, Role> implements RoleService {

    private final RoleMenuMapper roleMenuMapper;

    private final MenuService menuService;

    @Override
    public Set<Role> getUserRoles(Long userId) {
        return baseMapper.getUserRoles(userId);
    }

    @Override
    public List<GrantedAuthority> getUserGrantedAuthority(UserDto userDto) {
        Set<String> permissions = new HashSet<>();
        // 如果是管理员直接返回
        if (Constants.BOOL_TRUE.equals(userDto.getType())) {
            permissions.add("admin");
            return permissions.stream().map(PermissionGrantedAuthority::new)
                    .collect(Collectors.toList());
        }
        Set<Long> roleIds = userDto.getRoles().stream().map(RoleDto::getId).collect(Collectors.toSet());
        Set<MenuDto> menus = menuService.getMenusByRoleId(roleIds);
        permissions = menus.stream().filter(menu -> StringUtils.isNotBlank(menu.getPermission()))
                .map(MenuDto::getPermission).collect(Collectors.toSet());
        return permissions.stream().map(PermissionGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public Page<RoleDto> queryPage(RoleQueryDto queryDto, Page page) {
        Page rpage = Page.fromMybatisPlusPage(baseMapper.selectPage(page.toMybatisPlusPage(),
                QueryUtil.bulid(queryDto)));
        baseMapstruct.toDto(rpage.getRecord());
        return rpage;
    }

    @Override
    public List<RoleDto> query(RoleQueryDto queryDto) {
        return baseMapstruct.toDto(baseMapper.selectList(QueryUtil.bulid(queryDto)));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public RoleDto queryById(Serializable id) {
        RoleDto dto = super.queryById(id);
        dto.setMenus(menuService.getMenusByRoleId(Collections.singleton(dto.getId())));
        return dto;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public RoleDto create(RoleDto dto) {
        Role role = baseMapstruct.toEntity(dto);
        baseMapper.insert(role);
        for (MenuDto menuDto : dto.getMenus()) {
            RoleMenu rm = new RoleMenu(role.getId(), menuDto.getId());
            roleMenuMapper.insert(rm);
        }
        return baseMapstruct.toDto(role);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean update(RoleDto dto) {
        Role role = baseMapstruct.toEntity(dto);
        baseMapper.updateById(role);
        roleMenuMapper.delete(Wrappers.<RoleMenu>lambdaQuery().eq(RoleMenu::getRoleId, dto.getId()));
        for (MenuDto menuDto : dto.getMenus()) {
            RoleMenu rm = new RoleMenu(role.getId(), menuDto.getId());
            roleMenuMapper.insert(rm);
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteById(Long id) {
        baseMapper.deleteById(id);
        roleMenuMapper.delete(Wrappers.<RoleMenu>lambdaQuery().eq(RoleMenu::getRoleId, id));
        return true;
    }

    @Override
    public Set<RoleDto> getRoleByUserId(Long userId) {
        Set<Role> roles = baseMapper.getRoleByUserId(userId);
        return roles.stream().map(baseMapstruct::toDto).collect(Collectors.toSet());
    }
}
