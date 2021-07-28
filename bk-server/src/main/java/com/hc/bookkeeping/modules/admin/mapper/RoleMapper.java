package com.hc.bookkeeping.modules.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hc.bookkeeping.modules.admin.entity.Role;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Set;

/**
 * <p>
 * 角色表 Mapper 接口
 * </p>
 *
 * @author zjf
 * @since 2020-06-11
 */
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 用户id查询用户角色
     * @param userId 用户id
     * @return 角色集合
     */
    @Select("select * from sys_role where id in (select role_id from sys_user_role where user_id = #{userId})")
    Set<Role> getUserRoles(Long userId);

    /**
     * 用户id查询用户角色
     * @param userId 用户id
     * @return 角色集合
     */
    Set<Role> getRoleByUserId(@Param("userId") Long userId);
}
