package com.hc.bookkeeping.modules.bkeeping.service;

import cn.hutool.core.lang.Dict;
import com.hc.bookkeeping.common.base.BaseService;
import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.modules.bkeeping.dto.*;
import com.hc.bookkeeping.modules.bkeeping.entity.IncomeExpense;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Date;
import java.util.List;

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

    /**
     * 查询首页摘要信息
     * @param userId 用户id
     * @param days 查询最近几天的数据
     * @return
     */
    SummaryDto querySummary(Long userId, int days);

    /**
     * 按月,日统计收入支出信息
     * @return
     */
    BillResultDto querySumAmountPeriod(BillQueryDto billQueryDto);
}
