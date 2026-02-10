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

    /**
     * 设置用户额外的支出限额
     * @param userId 用户id
     * @param type 1:设置月限额(当月),2:设置年限额(当年)
     * @param expenseLimit 限额数值
     */
    void setAdditionalExpenseLimit(Long userId, String type, String expenseLimit);
}
