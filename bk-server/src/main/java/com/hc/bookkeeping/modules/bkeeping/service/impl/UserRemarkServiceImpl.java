package com.hc.bookkeeping.modules.bkeeping.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hc.bookkeeping.common.base.BaseServiceImpl;
import com.hc.bookkeeping.modules.bkeeping.dto.UserRemarkDto;
import com.hc.bookkeeping.modules.bkeeping.entity.UserRemark;
import com.hc.bookkeeping.modules.bkeeping.mapper.UserRemarkMapper;
import com.hc.bookkeeping.modules.bkeeping.mapstruct.UserRemarkMapstruct;
import com.hc.bookkeeping.modules.bkeeping.service.UserRemarkService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户常用备注表 服务实现类
 * </p>
 *
 * @author zjf
 * @since 2023-06-13
 */
@Service
public class UserRemarkServiceImpl extends BaseServiceImpl<UserRemarkMapstruct, UserRemarkDto, UserRemarkMapper, UserRemark> implements UserRemarkService {

    @Override
    public UserRemarkDto create(UserRemarkDto dto) {
        UserRemark e = baseMapstruct.toEntity(dto);
        //查询是否有记录,有替换,没有添加
        UserRemark one = baseMapper.selectOne(new LambdaQueryWrapper<UserRemark>().eq(UserRemark::getUserId, e.getUserId())
        .eq(UserRemark::getRemark, e.getRemark()));
        if(one != null) {
            baseMapper.updateById(one);
            e = one;
        } else {
            baseMapper.insert(e);
        }
        return baseMapstruct.toDto(e);
    }
}
