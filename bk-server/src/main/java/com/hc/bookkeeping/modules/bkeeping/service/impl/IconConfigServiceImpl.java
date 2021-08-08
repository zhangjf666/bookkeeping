package com.hc.bookkeeping.modules.bkeeping.service.impl;

import com.hc.bookkeeping.common.base.BaseServiceImpl;
import com.hc.bookkeeping.modules.bkeeping.dto.IconConfigDto;
import com.hc.bookkeeping.modules.bkeeping.entity.IconConfig;
import com.hc.bookkeeping.modules.bkeeping.mapper.IconConfigMapper;
import com.hc.bookkeeping.modules.bkeeping.mapstruct.IconConfigMapstruct;
import com.hc.bookkeeping.modules.bkeeping.service.IconConfigService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 图标配置表 服务实现类
 * </p>
 *
 * @author zjf
 * @since 2021-08-08
 */
@Service
public class IconConfigServiceImpl extends BaseServiceImpl<IconConfigMapstruct, IconConfigDto,IconConfigMapper, IconConfig> implements IconConfigService {

}
