package com.hc.bookkeeping.modules.bkeeping.mapper;

import cn.hutool.core.lang.Dict;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hc.bookkeeping.modules.bkeeping.entity.IncomeExpense;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 收入支出表 Mapper 接口
 * </p>
 *
 * @author zjf
 * @since 2021-07-27
 */
public interface IncomeExpenseMapper extends BaseMapper<IncomeExpense> {

    /**
     * 查询某一时间段内总收入支出
     * @param beginDate
     * @param endDate
     * @return
     */
    List<Dict> querySumAmount(Long userId, Date beginDate, Date endDate, List<Long> classify);

    /**
     * 按月统计总收入支出
     * @param beginDate
     * @param endDate
     * @return
     */
    List<Dict> querySumAmountMonthly(Long userId, Date beginDate, Date endDate, List<Long> classify);

    /**
     * 按日统计总收入支出
     * @param beginDate
     * @param endDate
     * @return
     */
    List<Dict> querySumAmountDayly(Long userId, Date beginDate, Date endDate,List<Long> classify);

    /**
     * 按分类统计总收入支出
     * @param beginDate
     * @param endDate
     * @param classify
     * @return
     */
    List<Dict> queryReportAmount(Long userId, Date beginDate, Date endDate, List<Long> classify);
}
