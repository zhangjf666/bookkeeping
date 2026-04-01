package com.hc.bookkeeping.modules.admin.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hc.bookkeeping.common.base.BaseServiceImpl;
import com.hc.bookkeeping.common.exception.DataNotExsitException;
import com.hc.bookkeeping.common.model.BoolEnum;
import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.common.utils.QueryUtil;
import com.hc.bookkeeping.modules.admin.dto.RoleDto;
import com.hc.bookkeeping.modules.admin.dto.UserDto;
import com.hc.bookkeeping.modules.admin.dto.UserQueryDto;
import com.hc.bookkeeping.modules.admin.entity.Role;
import com.hc.bookkeeping.modules.admin.entity.User;
import com.hc.bookkeeping.modules.admin.entity.UserRole;
import com.hc.bookkeeping.modules.admin.mapper.UserMapper;
import com.hc.bookkeeping.modules.admin.mapper.UserRoleMapper;
import com.hc.bookkeeping.modules.admin.mapstruct.UserMapstruct;
import com.hc.bookkeeping.modules.admin.service.RoleService;
import com.hc.bookkeeping.modules.admin.service.UserService;
import com.hc.bookkeeping.modules.bkeeping.entity.AccountBook;
import com.hc.bookkeeping.modules.bkeeping.entity.Classify;
import com.hc.bookkeeping.modules.bkeeping.entity.UserConfig;
import com.hc.bookkeeping.modules.bkeeping.mapper.AccountBookMapper;
import com.hc.bookkeeping.modules.bkeeping.mapper.ClassifyMapper;
import com.hc.bookkeeping.modules.bkeeping.mapper.UserConfigMapper;
import com.hc.bookkeeping.modules.bkeeping.model.BillType;
import com.hc.bookkeeping.modules.security.dto.RegisterUserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author zjf
 * @since 2020-06-11
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends BaseServiceImpl<UserMapstruct, UserDto, UserMapper, User> implements UserService {
    private static String DEFAULT_PASSWORD = "123456";

    private final UserRoleMapper userRoleMapper;
    
    private final RoleService roleService;
    
    private final UserConfigMapper userConfigMapper;

    private final AccountBookMapper accountBookMapper;

    private final ClassifyMapper classifyMapper;

    @Override
    public UserDto findByUsername(String username) {
        User user = baseMapper.findByUsername(username);
        if(user == null){
            throw new DataNotExsitException();
        }
        return baseMapstruct.toDto(user);
    }

    @Override
    public Page<UserDto> queryPage(UserQueryDto queryDto, Page page) {
        Page rpage = Page.fromMybatisPlusPage(baseMapper.selectPage(page.toMybatisPlusPage(), QueryUtil.bulid(queryDto)));
        rpage.setRecord(baseMapstruct.toDto(rpage.getRecord()));
        return rpage;
    }

    @Override
    public List<UserDto> query(UserQueryDto queryDto) {
        return baseMapstruct.toDto(baseMapper.selectList(QueryUtil.bulid(queryDto)));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public UserDto queryById(Serializable id) {
        UserDto dto = super.queryById(id);
        dto.setRoles(roleService.getRoleByUserId((Long) id));
        return dto;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public UserDto create(UserDto dto) {
        User user = baseMapstruct.toEntity(dto);
        user.setPassword(new BCryptPasswordEncoder().encode(DEFAULT_PASSWORD));
        baseMapper.insert(user);
        for (RoleDto roleDto:dto.getRoles()) {
            UserRole ur = new UserRole(user.getId(),roleDto.getId());
            userRoleMapper.insert(ur);
        }
        return baseMapstruct.toDto(user);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean update(UserDto dto) {
        baseMapper.updateById(baseMapstruct.toEntity(dto));
        if(dto.getRoles() != null){
            userRoleMapper.delete(Wrappers.<UserRole>lambdaQuery().eq(UserRole::getUserId,dto.getId()));
            for (RoleDto roleDto:dto.getRoles()) {
                UserRole ur = new UserRole(dto.getId(),roleDto.getId());
                userRoleMapper.insert(ur);
            }
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteById(Long id) {
        baseMapper.deleteById(id);
        userRoleMapper.delete(Wrappers.<UserRole>lambdaQuery().eq(UserRole::getUserId, id));
        return true;
    }

    @Override
    public boolean checkExist(String userName) {
        return count(Wrappers.<User>lambdaQuery().eq(User::getUsername, userName.trim())) > 0;
    }
}
