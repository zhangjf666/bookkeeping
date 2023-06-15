package com.hc.bookkeeping.modules.bkeeping.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hc.bookkeeping.common.base.BaseServiceImpl;
import com.hc.bookkeeping.common.model.BoolEnum;
import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.common.utils.QueryUtil;
import com.hc.bookkeeping.modules.bkeeping.dto.*;
import com.hc.bookkeeping.modules.bkeeping.entity.IncomeExpense;
import com.hc.bookkeeping.modules.bkeeping.entity.UserSearch;
import com.hc.bookkeeping.modules.bkeeping.mapper.IncomeExpenseMapper;
import com.hc.bookkeeping.modules.bkeeping.mapper.UserSearchMapper;
import com.hc.bookkeeping.modules.bkeeping.mapstruct.IncomeExpenseMapstruct;
import com.hc.bookkeeping.modules.bkeeping.model.BillType;
import com.hc.bookkeeping.modules.bkeeping.service.ClassifyService;
import com.hc.bookkeeping.modules.bkeeping.service.IncomeExpenseService;
import com.hc.bookkeeping.modules.bkeeping.service.UserRemarkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
@Slf4j
@Service
public class IncomeExpenseServiceImpl extends BaseServiceImpl<IncomeExpenseMapstruct, IncomeExpenseDto, IncomeExpenseMapper, IncomeExpense> implements IncomeExpenseService {
    @Autowired
    private ClassifyService classifyService;
    @Autowired
    private UserSearchMapper userSearchMapper;
    @Autowired
    private UserRemarkService userRemarkService;

    @Override
    public List<IncomeExpenseDto> queryList(IncomeExpenseQueryDto queryDto) {
        List<IncomeExpenseDto> list = queryList(QueryUtil.bulid(queryDto));
        fillClassify(list);
        //记录搜索记录
        try {
            if(queryDto.getUserId() != null && StringUtils.isNotBlank(queryDto.getRemark())) {
                Integer count = userSearchMapper.selectCount(Wrappers.<UserSearch>lambdaQuery()
                        .eq(UserSearch::getUserId, queryDto.getUserId())
                        .eq(UserSearch::getContent, queryDto.getRemark()));
                if(count <= 0) {
                    UserSearch us = new UserSearch();
                    us.setUserId(queryDto.getUserId());
                    us.setContent(queryDto.getRemark());
                    userSearchMapper.insert(us);
                }
            }
        } catch (Exception ex){
            log.error(StrUtil.format("{} 保存搜索记录失败:{}",queryDto.getUserId(), ex.getMessage()), ex);
        }
        return list;
    }

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
        List<Dict> sumAmount = baseMapper.querySumAmount(userId, beginDate, endDate, null);
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
        List<IncomeExpenseDto> list = queryList(Wrappers.<IncomeExpense>lambdaQuery()
                .eq(IncomeExpense::getUserId, userId)
                .ge(IncomeExpense::getCreateTime, detail)
                .orderByDesc(IncomeExpense::getDate,IncomeExpense::getCreateTime));
        fillClassify(list);

        summaryDto.setExpenseAmount(expense);
        summaryDto.setIncomeAmount(income);
        summaryDto.setIncomeExpenseList(list);
        return summaryDto;
    }

    @Override
    public BillResultDto querySumAmountPeriod(BillQueryDto billQueryDto) {
        BillResultDto billResultDto = new BillResultDto();
        //确定开始结束时间
        Date beginDate = DateUtil.date(billQueryDto.getBeginDate());
        Date endDate = DateUtil.date(billQueryDto.getEndDate());
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
        List<Dict> sumAmount = baseMapper.querySumAmount(billQueryDto.getUserId(), beginDate, endDate, billQueryDto.getClassifyList());
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
                    BigDecimal amount = data.getBigDecimal("amount");
                    ies.set("percent", NumberUtil.round(amount.divide(income, 4,BigDecimal.ROUND_HALF_UP).doubleValue() * 100, 2));
                    ies.set("income", amount);
                } else {
                    BigDecimal amount = data.getBigDecimal("amount");
                    ies.set("percent", NumberUtil.round(amount.divide(expense, 4,BigDecimal.ROUND_HALF_UP).doubleValue() * 100,2));
                    ies.set("expense", data.getBigDecimal("amount"));
                }
                //获取图标跟名称
                ies.set("num", data.getInt("num"));
                ClassifyDto mclassify = classifyService.queryUserClassifyById(billQueryDto.getUserId(), data.getLong("classify"));
                if(mclassify != null) {
                    ies.set("classifyName", mclassify.getName());
                    ies.set("classifyImage", mclassify.getImage());
                }
                ies.set("classify", data.getStr("classify"));
                incomeExpenseSum.set(data.getStr("classify"), ies);
            }
        }
        billResultDto.setIncomeExpenseSum(incomeExpenseSum);

        //查询收支明细
        List<IncomeExpenseDto> list = queryList(Wrappers.<IncomeExpense>lambdaQuery().ge(IncomeExpense::getDate, beginDate)
                .eq(IncomeExpense::getUserId, billQueryDto.getUserId())
                .le(IncomeExpense::getDate, endDate)
                .in(!billQueryDto.getClassifyList().isEmpty(), IncomeExpense::getMainClassify, billQueryDto.getClassifyList())
                .orderByDesc(IncomeExpense::getDate,IncomeExpense::getCreateTime));
        fillClassify(list);
        billResultDto.setIncomeExpenseList(list);
        return billResultDto;
    }

    private void fillClassify(List<IncomeExpenseDto> list){
        for (IncomeExpenseDto dto:list) {
            if(dto.getMainClassify() != null) {
                ClassifyDto mclassify = classifyService.queryUserClassifyById(dto.getUserId(), dto.getMainClassify());
                if(mclassify != null) {
                    dto.setMainClassifyName(mclassify.getName());
                    dto.setMainClassifyImage(mclassify.getImage());
                }
            }
            if(dto.getSubClassify() != null) {
                ClassifyDto sclassify = classifyService.queryUserClassifyById(dto.getUserId(),dto.getSubClassify());
                if(sclassify != null) {
                    dto.setSubClassifyName(sclassify.getName());
                    dto.setSubClassifyImage(sclassify.getImage());
                }
            }
        }
    }

    @Override
    public IncomeExpenseDto create(IncomeExpenseDto dto) {
        if(dto.getIsAddRemark() == BoolEnum.True && StringUtils.isNotBlank(dto.getRemark())){
            //remark添加到常用备注
            UserRemarkDto ur = new UserRemarkDto();
            ur.setUserId(dto.getUserId());
            ur.setRemark(dto.getRemark());
            userRemarkService.create(ur);
        }
        return super.create(dto);
    }

    @Override
    public boolean update(IncomeExpenseDto dto) {
        if(dto.getIsAddRemark() == BoolEnum.True && StringUtils.isNotBlank(dto.getRemark())){
            //remark添加到常用备注
            UserRemarkDto ur = new UserRemarkDto();
            ur.setUserId(dto.getUserId());
            ur.setRemark(dto.getRemark());
            userRemarkService.create(ur);
        }
        return super.update(dto);
    }
}
