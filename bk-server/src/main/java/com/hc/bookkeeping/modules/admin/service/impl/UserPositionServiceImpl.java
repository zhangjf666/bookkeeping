package com.hc.bookkeeping.modules.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hc.bookkeeping.modules.admin.entity.UserPosition;
import com.hc.bookkeeping.modules.admin.mapper.UserPositionMapper;
import com.hc.bookkeeping.modules.admin.service.UserPositionService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户-职位表 服务实现类
 * </p>
 *
 * @author zjf
 * @since 2020-06-11
 */
@Service
public class UserPositionServiceImpl extends ServiceImpl<UserPositionMapper, UserPosition> implements UserPositionService {

}
