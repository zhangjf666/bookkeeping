package com.hc.bookkeeping.modules.bkeeping.mapper;

import cn.hutool.core.lang.Dict;
import com.hc.bookkeeping.modules.bkeeping.dto.IncomeExpenseDto;
import com.hc.bookkeeping.modules.bkeeping.entity.IncomeExpense;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.math.BigDecimal;
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
     * @param type
     * @return
     */
    BigDecimal querySumAmount(Date beginDate, Date endDate, String type);
}
