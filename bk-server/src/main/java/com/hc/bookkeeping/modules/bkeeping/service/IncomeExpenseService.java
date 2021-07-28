package com.hc.bookkeeping.modules.bkeeping.service;

import com.hc.bookkeeping.common.base.BaseService;
import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.modules.bkeeping.dto.IncomeExpenseDto;
import com.hc.bookkeeping.modules.bkeeping.dto.IncomeExpenseQueryDto;
import com.hc.bookkeeping.modules.bkeeping.entity.IncomeExpense;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 收入支出表 服务类
 * </p>
 *
 * @author zjf
 * @since 2021-07-27
 */
public interface IncomeExpenseService extends BaseService<IncomeExpenseDto, IncomeExpense> {

    /**
     * 分页查询
     * @param queryDto 查询条件
     * @param page 分页
     * @return
     */
    Page queryPage(IncomeExpenseQueryDto queryDto, Page page);
}
