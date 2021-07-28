package com.hc.bookkeeping.modules.admin.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.hc.bookkeeping.common.annotation.Log;
import com.hc.bookkeeping.common.constants.Constants;
import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.common.model.Response;
import com.hc.bookkeeping.common.support.valid.Insert;
import com.hc.bookkeeping.common.support.valid.Update;
import com.hc.bookkeeping.common.utils.SpringSecurityUtil;
import com.hc.bookkeeping.common.utils.TreeUtil;
import com.hc.bookkeeping.modules.admin.dto.MenuDto;
import com.hc.bookkeeping.modules.admin.dto.MenuQueryDto;
import com.hc.bookkeeping.modules.admin.dto.RoleDto;
import com.hc.bookkeeping.modules.admin.dto.vo.MenuVo;
import com.hc.bookkeeping.modules.admin.dto.vo.UserMenuRoute;
import com.hc.bookkeeping.modules.admin.entity.Menu;
import com.hc.bookkeeping.modules.admin.service.MenuService;
import com.hc.bookkeeping.modules.admin.service.RoleService;
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
 * @Date: 2020/7/1 8:58
 */
@Slf4j
@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
@Api(tags = "菜单接口")
public class MenuController {

    private final MenuService menuService;

    private final RoleService roleService;

    @Log("查询菜单")
    @ApiOperation("查询菜单")
    @GetMapping
    @PreAuthorize("@ph.check('system:menu:list')")
    public Response getMenu(@Validated MenuQueryDto queryDto, Page pageable) {
        pageable.setPageSize(9999);
        Page page = menuService.queryPage(queryDto, pageable);
        List<MenuDto> dtos = page.getRecord();
        List<MenuVo> volist =
                dtos.stream().map(dto -> BeanUtil.toBean(dto, MenuVo.class)).collect(Collectors.toList());
        for (MenuVo vo: volist) {
            vo.setHasChildren(menuService.count(new QueryWrapper<Menu>().lambda().eq(Menu::getPid,vo.getId())) > 0);
        }
        page.setRecord(volist);
        return Response.ok(page);
    }

    @Log("创建菜单")
    @ApiOperation("创建菜单")
    @PostMapping
    @PreAuthorize("@ph.check('system:menu:add')")
    public Response create(@Validated(Insert.class) @RequestBody MenuDto dto) {
        menuService.create(dto);
        return Response.ok();
    }

    @Log("编辑菜单")
    @ApiOperation("编辑菜单")
    @PutMapping
    @PreAuthorize("@ph.check('system:menu:edit')")
    public Response update(@Validated(Update.class) @RequestBody MenuDto dto) {
        menuService.update(dto);
        return Response.ok();
    }

    @Log("删除菜单")
    @ApiOperation("删除菜单")
    @DeleteMapping
    @PreAuthorize("@ph.check('system:menu:del')")
    public Response delete(@RequestBody Set<Long> ids) {
        menuService.removeByIds(ids);
        return Response.ok();
    }

    @ApiOperation("获取用户菜单")
    @GetMapping("/userMenu")
    public Response getUserMenu() {
        Set<RoleDto> roles = roleService.getRoleByUserId(SpringSecurityUtil.getCurrentUserId());
        Set<Long> roleId = roles.stream().map(RoleDto::getId).collect(Collectors.toSet());
        Set<MenuDto> userMenus = menuService.getMenusByRoleId(roleId);
        List<UserMenuRoute> routes = userMenus.stream()
                .filter(dto -> !Menu.TYPE_BUTTON.equals(dto.getType()))
                .map(UserMenuRoute::new)
                .collect(Collectors.toList());
        return Response.ok(buildMenus(TreeUtil.build(routes, Constants.TREE_ROOT)));
    }

    @Log("获取所有菜单树")
    @ApiOperation("获取所有菜单树")
    @GetMapping("/tree")
    @PreAuthorize("@ph.check('system:menu:list')")
    public Response getMenuTree() {
        List<MenuDto> dtos = menuService.query(new MenuQueryDto());
        List<MenuVo> volist =
                dtos.stream().map(dto -> BeanUtil.toBean(dto, MenuVo.class)).collect(Collectors.toList());
        return Response.ok(TreeUtil.build(volist, Constants.TREE_ROOT));
    }

    /**
     * 处理路由
     *
     * @param menuTreeList 传入的树节点列表
     * @return
     */
    private List<UserMenuRoute> buildMenus(List<UserMenuRoute> menuTreeList) {
        menuTreeList.forEach(menu -> {
                    if (menu != null) {
                        List<UserMenuRoute> menuChildList = menu.getChildren();
                        // 一级目录需要加斜杠，不然会报警告
                        String path = menu.getPath();
                        menu.setPath(menu.getPid().equals(Constants.TREE_ROOT) ? "/" + path :path);
                        if (CollUtil.isNotEmpty(menuChildList)) {
                            menu.setAlwaysShow(true);
                            menu.setRedirect("noRedirect");
                            menu.setChildren(buildMenus(menuChildList));
                            // 处理是一级菜单并且没有子菜单的情况
                        } else if (menu.getPid().equals(Constants.TREE_ROOT)) {
                            UserMenuRoute route = new UserMenuRoute();
                            route.setMeta(menu.getMeta());
                            // 非外链
                            if (!menu.getIframe()) {
                                route.setPath("index");
                                route.setName(menu.getName());
                                route.setComponent(menu.getComponent());
                            } else {
                                route.setPath(path);
                            }
                            menu.setName(null);
                            menu.setMeta(null);
                            menu.setComponent("Layout");
                            menu.setChildren(Lists.newArrayList(route));
                        }
                    }
                }
        );
        return menuTreeList;
    }

    @Log("查询菜单")
    @ApiOperation("查询菜单:根据ID获取同级与上级数据")
    @PostMapping("/superior")
    @PreAuthorize("@ph.check('system:menu:list')")
    public Response getSuperior(@RequestBody List<Long> ids) {
        Set<MenuDto> menuDtos  = new LinkedHashSet<>();
        for (Long id : ids) {
            MenuDto menuDto = menuService.queryById(id);
            List<MenuDto> menus = menuService.getSuperior(menuDto, new ArrayList<>());
            menuDtos.addAll(menus);
        }
        List<MenuVo> volist =
                menuDtos.stream().map(dto -> BeanUtil.toBean(dto,MenuVo.class)).collect(Collectors.toList());
        volist = TreeUtil.build(volist, Constants.TREE_ROOT);
        menuService.superiorCheckChildren(volist);
        return Response.ok(volist);
    }
}
