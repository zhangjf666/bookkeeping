package com.hc.bookkeeping.modules.bkeeping.mapper;

import com.hc.bookkeeping.modules.bkeeping.entity.UserTag;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 用户标签表 Mapper 接口
 * </p>
 *
 * @author zjf
 * @since 2026-02-11
 */
public interface UserTagMapper extends BaseMapper<UserTag> {

    @Override
    int insert(UserTag entity);
}
