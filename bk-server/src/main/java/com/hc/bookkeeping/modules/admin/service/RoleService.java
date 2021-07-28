package com.hc.bookkeeping.modules.admin.service;

import com.hc.bookkeeping.common.base.BaseService;
import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.modules.admin.dto.RoleDto;
import com.hc.bookkeeping.modules.admin.dto.RoleQueryDto;
import com.hc.bookkeeping.modules.admin.dto.UserDto;
import com.hc.bookkeeping.modules.admin.entity.Role;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * 角色表 服务类
 * </p>
 *
 * @author zjf
 * @since 2020-06-11
 */
public interface RoleService extends BaseService<RoleDto, Role> {

    /**
     * 查询用户角色
     * @param userId 用户id
     * @return
     */
    Set<Role> getUserRoles(Long userId);

    /**
     * 查询用户权限
     * @param userDto 用户Dto
     * @return
     */
    List<GrantedAuthority> getUserGrantedAuthority(UserDto userDto);

    /**
     * 分页查询
     * @param queryDto 查询条件
     * @param page 分页
     * @return
     */
    Page queryPage(RoleQueryDto queryDto, Page page);

    /**
     * 查询
     * @param queryDto 查询条件
     * @return
     */
    List<RoleDto> query(RoleQueryDto queryDto);

    @Override
    RoleDto queryById(Serializable id);

    /**
     * 保存角色
     * @param dto 角色Dto
     * @return
     */
    @Override
    RoleDto create(RoleDto dto);

    /**
     * 更新角色
     * @param dto 角色Dto
     * @return
     */
    @Override
    boolean update(RoleDto dto);

    /**
     * 删除角色
     * @param id 角色id
     * @return
     */
    boolean deleteById(Long id);

    /**
     * 获取用户对应的角色
     * @param userId 用户id
     * @return
     */
    Set<RoleDto> getRoleByUserId(Long userId);
}
