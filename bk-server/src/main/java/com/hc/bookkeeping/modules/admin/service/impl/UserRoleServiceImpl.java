package com.hc.bookkeeping.modules.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hc.bookkeeping.modules.admin.entity.UserRole;
import com.hc.bookkeeping.modules.admin.mapper.UserRoleMapper;
import com.hc.bookkeeping.modules.admin.service.UserRoleService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户-角色表 服务实现类
 * </p>
 *
 * @author zjf
 * @since 2020-06-11
 */
@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements UserRoleService {

}
