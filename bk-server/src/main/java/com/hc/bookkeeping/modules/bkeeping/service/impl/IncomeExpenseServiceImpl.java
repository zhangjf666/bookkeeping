package com.hc.bookkeeping.modules.bkeeping.service.impl;

import com.hc.bookkeeping.common.base.BaseServiceImpl;
import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.common.utils.QueryUtil;
import com.hc.bookkeeping.modules.bkeeping.dto.IncomeExpenseDto;
import com.hc.bookkeeping.modules.bkeeping.dto.IncomeExpenseQueryDto;
import com.hc.bookkeeping.modules.bkeeping.entity.IncomeExpense;
import com.hc.bookkeeping.modules.bkeeping.mapper.IncomeExpenseMapper;
import com.hc.bookkeeping.modules.bkeeping.mapstruct.IncomeExpenseMapstruct;
import com.hc.bookkeeping.modules.bkeeping.service.IncomeExpenseService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 收入支出表 服务实现类
 * </p>
 *
 * @author zjf
 * @since 2021-07-27
 */
@Service
public class IncomeExpenseServiceImpl extends BaseServiceImpl<IncomeExpenseMapstruct, IncomeExpenseDto, IncomeExpenseMapper, IncomeExpense> implements IncomeExpenseService {

    @Override
    public Page queryPage(IncomeExpenseQueryDto queryDto, Page page) {
        return queryPage(page, QueryUtil.bulid(queryDto));
    }
}
