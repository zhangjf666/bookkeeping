package com.hc.bookkeeping.modules.bkeeping.mapstruct;

import com.hc.bookkeeping.common.base.BaseMapstruct;
import com.hc.bookkeeping.modules.bkeeping.dto.IncomeExpenseDto;
import com.hc.bookkeeping.modules.bkeeping.entity.IncomeExpense;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IncomeExpenseMapstruct extends BaseMapstruct<IncomeExpenseDto, IncomeExpense> {
}
