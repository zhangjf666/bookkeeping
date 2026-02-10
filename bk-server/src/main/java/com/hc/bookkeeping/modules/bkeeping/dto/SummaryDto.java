package com.hc.bookkeeping.modules.bkeeping.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 首页摘要信息
 */
@Data
public class SummaryDto {

    private BigDecimal expenseAmount;

    private BigDecimal incomeAmount;

    private List<IncomeExpenseDto> incomeExpenseList;

    private BigDecimal expenseLimit;

    private BigDecimal expenseSurplus;
}
