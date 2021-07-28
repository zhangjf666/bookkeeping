package com.hc.bookkeeping.modules.bkeeping.service;

import com.hc.bookkeeping.common.base.BaseService;
import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.modules.bkeeping.dto.UserConfigDto;
import com.hc.bookkeeping.modules.bkeeping.dto.UserConfigQueryDto;
import com.hc.bookkeeping.modules.bkeeping.entity.UserConfig;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户配置表 服务类
 * </p>
 *
 * @author zjf
 * @since 2021-07-27
 */
public interface UserConfigService extends BaseService<UserConfigDto, UserConfig> {

    /**
     * 分页查询
     * @param queryDto 查询条件
     * @param page 分页
     * @return
     */
    Page queryPage(UserConfigQueryDto queryDto, Page page);
}
