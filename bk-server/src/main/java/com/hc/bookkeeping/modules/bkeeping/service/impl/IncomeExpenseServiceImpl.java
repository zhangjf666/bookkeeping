package com.hc.bookkeeping.modules.bkeeping.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Dict;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hc.bookkeeping.common.base.BaseServiceImpl;
import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.common.utils.QueryUtil;
import com.hc.bookkeeping.modules.bkeeping.dto.IncomeExpenseDto;
import com.hc.bookkeeping.modules.bkeeping.dto.IncomeExpenseQueryDto;
import com.hc.bookkeeping.modules.bkeeping.dto.SummaryDto;
import com.hc.bookkeeping.modules.bkeeping.entity.Classify;
import com.hc.bookkeeping.modules.bkeeping.entity.IncomeExpense;
import com.hc.bookkeeping.modules.bkeeping.mapper.ClassifyMapper;
import com.hc.bookkeeping.modules.bkeeping.mapper.IncomeExpenseMapper;
import com.hc.bookkeeping.modules.bkeeping.mapstruct.IncomeExpenseMapstruct;
import com.hc.bookkeeping.modules.bkeeping.model.BillType;
import com.hc.bookkeeping.modules.bkeeping.service.IncomeExpenseService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

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
    @Autowired
    private ClassifyMapper classifyMapper;

    @Override
    public Page queryPage(IncomeExpenseQueryDto queryDto, Page page) {
        return queryPage(page, QueryUtil.bulid(queryDto));
    }

    @Override
    public SummaryDto querySummary(Long userId, int days) {
        SummaryDto summaryDto = new SummaryDto();
        //查询本月收支情况
        Date beginDate = DateUtil.beginOfMonth(new Date());
        Date endDate = DateUtil.endOfMonth(new Date());
        //收入
        BigDecimal income = baseMapper.querySumAmount(beginDate, endDate, BillType.Income.getValue());
        //支出
        BigDecimal expense = baseMapper.querySumAmount(beginDate, endDate, BillType.Expense.getValue());
        //收支明细
        Date detail = DateUtil.beginOfDay(DateUtil.offsetDay(new Date(), -days));
        List<IncomeExpenseDto> list = queryList(Wrappers.<IncomeExpense>lambdaQuery().ge(IncomeExpense::getDate, detail).orderByDesc(IncomeExpense::getDate,IncomeExpense::getCreateTime));
        for (IncomeExpenseDto dto:list) {
            if(dto.getMainClassify() != null) {
                Classify mclassify = classifyMapper.selectById(dto.getMainClassify());
                if(mclassify != null) {
                    dto.setMainClassifyName(mclassify.getName());
                    dto.setMainClassifyImage(mclassify.getImage());
                }
            }
            if(dto.getSubClassify() != null) {
                Classify sclassify = classifyMapper.selectById(dto.getSubClassify());
                if(sclassify != null) {
                    dto.setSubClassifyName(sclassify.getName());
                    dto.setSubClassifyImage(sclassify.getImage());
                }
            }
        }

        summaryDto.setExpenseAmount(expense);
        summaryDto.setIncomeAmount(income);
        summaryDto.setIncomeExpenseList(list);
        return summaryDto;
    }
}
