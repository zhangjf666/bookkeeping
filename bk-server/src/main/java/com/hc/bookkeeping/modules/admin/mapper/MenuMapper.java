package com.hc.bookkeeping.modules.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hc.bookkeeping.modules.admin.entity.Menu;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * <p>
 * 菜单表 Mapper 接口
 * </p>
 *
 * @author zjf
 * @since 2020-06-11
 */
public interface MenuMapper extends BaseMapper<Menu> {

    @Select("select * from sys_menu where id in (select menu_id from sys_role_menu where role_id = #{roleId})")
    Set<Menu> getRoleMenus(Long roleId);

    LinkedHashSet<Menu> getMenusByRoleId(@Param("roleId") Set<Long> roleId);
}
