package com.hc.bookkeeping.modules.bkeeping.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Dict;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hc.bookkeeping.common.base.BaseServiceImpl;
import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.common.utils.QueryUtil;
import com.hc.bookkeeping.modules.bkeeping.dto.*;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.hc.bookkeeping.modules.bkeeping.constants.constants.*;

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
        //收入支出
        List<Dict> sumAmount = baseMapper.querySumAmount(userId, beginDate, endDate);
        BigDecimal expense = BigDecimal.ZERO;
        BigDecimal income = BigDecimal.ZERO;
        for (Dict dict: sumAmount) {
            if(dict.getInt("type") == 0){
                expense = dict.getBigDecimal("amount");
            } else {
                income = dict.getBigDecimal("amount");
            }
        };
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

    @Override
    public BillResultDto querySumAmountPeriod(BillQueryDto billQueryDto) {
        BillResultDto billResultDto = new BillResultDto();
        //确定开始结束时间
        Date beginDate = billQueryDto.getBeginDate();
        Date endDate = billQueryDto.getEndDate();
        if(SUM_PERIOD_MONTH.equals(billQueryDto.getMode())){
            beginDate = DateUtil.beginOfMonth(beginDate);
            endDate = DateUtil.endOfMonth(beginDate);
        } else if(SUM_PERIOD_YEAR.equals(billQueryDto.getMode())){
            beginDate = DateUtil.beginOfYear(beginDate);
            endDate = DateUtil.endOfYear(beginDate);
        } else {
            beginDate = DateUtil.beginOfDay(beginDate);
            endDate = DateUtil.endOfDay(endDate);
        }
        //查询收入支出统计
        List<Dict> sumAmount = baseMapper.querySumAmount(billQueryDto.getUserId(), beginDate, endDate);
        BigDecimal expense = BigDecimal.ZERO;
        BigDecimal income = BigDecimal.ZERO;
        for (Dict dict: sumAmount) {
            if(dict.getInt("type") == 0){
                expense = dict.getBigDecimal("amount");
            } else {
                income = dict.getBigDecimal("amount");
            }
        };
        billResultDto.setIncomeTotal(income);
        billResultDto.setExpenseTotal(expense);

        //查询统计信息
        //账单
        Dict incomeExpenseSum = Dict.create();
        if(SUM_MODE_BILL.equals(billQueryDto.getQueryMode())){
            //构建一个全日期列表
            List<DateTime> dateTimes = DateUtil.rangeToList(beginDate, endDate, DateField.DAY_OF_MONTH);
            for (DateTime date: dateTimes) {
                Dict dict = Dict.create();
                String dateStr = SUM_PERIOD_YEAR.equals(billQueryDto.getMode()) ? date.toString("yyyy-MM") : date.toString(DatePattern.NORM_DATE_PATTERN);
                dict.set("income", BigDecimal.ZERO).set("expense", BigDecimal.ZERO);
                incomeExpenseSum.set(dateStr, dict);
            }
            //查询数据
            List<Dict> datas;
            datas = SUM_PERIOD_YEAR.equals(billQueryDto.getMode()) ?
                    baseMapper.querySumAmountMonthly(billQueryDto.getUserId(), beginDate, endDate, billQueryDto.getClassifyList()) :
                    baseMapper.querySumAmountDayly(billQueryDto.getUserId(), beginDate, endDate, billQueryDto.getClassifyList());
            for (Dict data:datas) {
                Dict ies = (Dict) incomeExpenseSum.get(data.getStr("period"));
                if(BillType.Income.getValue().equals(data.getStr("type"))) {
                    ies.set("income", data.getBigDecimal("amount"));
                } else {
                    ies.set("expense", data.getBigDecimal("amount"));
                }
            }
        } else {
            //报表
            List<Dict> datas = baseMapper.queryReportAmount(billQueryDto.getUserId(), beginDate, endDate, billQueryDto.getClassifyList());
            for(Dict data: datas) {
                Dict ies = (Dict) incomeExpenseSum.get(data.getStr("classify"));
                if(ies == null){
                    ies = Dict.create();
                }
                if(BillType.Income.getValue().equals(data.getStr("type"))) {
                    ies.set("income", data.getBigDecimal("amount"));
                } else {
                    ies.set("expense", data.getBigDecimal("amount"));
                }
                incomeExpenseSum.set(data.getStr("classify"), ies);
            }
        }
        billResultDto.setIncomeExpenseSum(incomeExpenseSum);

        //查询收支明细
        List<IncomeExpenseDto> list = queryList(Wrappers.<IncomeExpense>lambdaQuery().ge(IncomeExpense::getDate, beginDate).le(IncomeExpense::getDate, endDate).orderByDesc(IncomeExpense::getDate,IncomeExpense::getCreateTime));
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
        billResultDto.setIncomeExpenseList(list);
        return billResultDto;
    }
}
