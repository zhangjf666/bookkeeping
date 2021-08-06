package com.hc.bookkeeping.modules.bkeeping.dto;

import cn.hutool.core.lang.Dict;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 账单跟报表查询结果实体
 */
@Data
public class BillResultDto {

    /**
     * 总支出
     */
    private BigDecimal expenseTotal;

    /**
     * 总收入
     */
    private BigDecimal incomeTotal;

    /**
     * 收入支出统计列表
     */
    private Dict incomeExpenseSum;

    /**
     * 收支详细列表
     */
    private List<IncomeExpenseDto> incomeExpenseList;
}
