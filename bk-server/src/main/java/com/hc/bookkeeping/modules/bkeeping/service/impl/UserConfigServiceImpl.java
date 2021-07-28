package com.hc.bookkeeping.modules.bkeeping.service.impl;

import com.hc.bookkeeping.common.base.BaseServiceImpl;
import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.common.utils.QueryUtil;
import com.hc.bookkeeping.modules.bkeeping.dto.UserConfigDto;
import com.hc.bookkeeping.modules.bkeeping.dto.UserConfigQueryDto;
import com.hc.bookkeeping.modules.bkeeping.entity.UserConfig;
import com.hc.bookkeeping.modules.bkeeping.mapper.UserConfigMapper;
import com.hc.bookkeeping.modules.bkeeping.mapstruct.UserConfigMapstruct;
import com.hc.bookkeeping.modules.bkeeping.service.UserConfigService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户配置表 服务实现类
 * </p>
 *
 * @author zjf
 * @since 2021-07-27
 */
@Service
public class UserConfigServiceImpl extends BaseServiceImpl<UserConfigMapstruct, UserConfigDto, UserConfigMapper, UserConfig> implements UserConfigService {

    @Override
    public Page queryPage(UserConfigQueryDto queryDto, Page page) {
        return queryPage(page, QueryUtil.bulid(queryDto));
    }
}
