package com.hc.bookkeeping.modules.admin.service;

import com.hc.bookkeeping.common.base.BaseService;
import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.modules.admin.dto.UserDto;
import com.hc.bookkeeping.modules.admin.dto.UserQueryDto;
import com.hc.bookkeeping.modules.admin.entity.User;
import com.hc.bookkeeping.modules.security.dto.RegisterUserDto;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author zjf
 * @since 2020-06-11
 */
public interface UserService extends BaseService<UserDto, User> {

    /**
     * 用户名查询用户Dto
     * @param username 用户名 
     * @return
     */
    UserDto findByUsername(String username);

    /**
     * 分页查询
     * @param queryDto 查询条件
     * @param page 分页
     * @return
     */
    Page queryPage(UserQueryDto queryDto, Page page);



    /**
     * 查询
     * @param queryDto 查询条件
     * @return
     */
    List<UserDto> query(UserQueryDto queryDto);

    @Override
    UserDto queryById(Serializable id);

    /**
     * 保存用户
     * @param dto 用户Dto
     * @return
     */
    @Override
    UserDto create(UserDto dto);

    /**
     * 更新用户
     * @param dto 用户Dto
     * @return
     */
    @Override
    boolean update(UserDto dto);

    /**
     * 删除用户
     * @param id 用户id
     * @return
     */
    boolean deleteById(Long id);

    /**
     * 查询用户名是否已存在
     * @param userName 用户名
     * @return
     */
    boolean checkExist(String userName);

    /**
     * 注册用户
     * @param dto
     * @return
     */
    boolean registerUser(RegisterUserDto dto);
}
