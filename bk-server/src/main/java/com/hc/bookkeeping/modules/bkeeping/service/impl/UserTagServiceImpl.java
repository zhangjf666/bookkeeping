package com.hc.bookkeeping.modules.bkeeping.service.impl;

import com.hc.bookkeeping.common.base.BaseServiceImpl;
import com.hc.bookkeeping.modules.bkeeping.dto.UserTagDto;
import com.hc.bookkeeping.modules.bkeeping.entity.UserTag;
import com.hc.bookkeeping.modules.bkeeping.mapper.UserTagMapper;
import com.hc.bookkeeping.modules.bkeeping.mapstruct.UserTagMapstruct;
import com.hc.bookkeeping.modules.bkeeping.service.UserTagService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户标签表 服务实现类
 * </p>
 *
 * @author zjf
 * @since 2026-02-11
 */
@Service
public class UserTagServiceImpl extends BaseServiceImpl<UserTagMapstruct, UserTagDto, UserTagMapper, UserTag> implements UserTagService {
    @Override
    public boolean update(UserTagDto dto) {
        UserTag entity = baseMapstruct.toEntity(dto);
        //不更新code
        entity.setCode(null);
        return baseMapper.updateById(entity) >= 1;
    }
}
