package com.hc.bookkeeping.modules.bkeeping.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hc.bookkeeping.common.base.BaseServiceImpl;
import com.hc.bookkeeping.common.dto.ExcelExportDto;
import com.hc.bookkeeping.common.exception.BusinessException;
import com.hc.bookkeeping.common.model.BoolEnum;
import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.common.utils.ExcelExportUtils;
import com.hc.bookkeeping.common.utils.QueryUtil;
import com.hc.bookkeeping.modules.bkeeping.dto.AccountBookDto;
import com.hc.bookkeeping.modules.bkeeping.constants.ExpenseLimitShowType;
import com.hc.bookkeeping.modules.bkeeping.dto.*;
import com.hc.bookkeeping.modules.bkeeping.entity.*;
import com.hc.bookkeeping.modules.bkeeping.mapper.IncomeExpenseMapper;
import com.hc.bookkeeping.modules.bkeeping.mapper.UserSearchMapper;
import com.hc.bookkeeping.modules.bkeeping.mapstruct.IncomeExpenseMapstruct;
import com.hc.bookkeeping.modules.bkeeping.model.BillType;
import com.hc.bookkeeping.modules.bkeeping.service.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletResponse;

import static com.hc.bookkeeping.modules.bkeeping.constants.Constants.*;

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
    private AccountBookService accountBookService;
    @Autowired
    private ClassifyService classifyService;
    @Autowired
    private UserSearchMapper userSearchMapper;
    @Autowired
    private UserConfigService userConfigService;
    @Autowired
    private UserRemarkService userRemarkService;
    @Autowired
    private UserTagService userTagService;

    @Override
    public List<IncomeExpenseDto> queryList(IncomeExpenseQueryDto queryDto) {
        List<IncomeExpenseDto> list = queryList(QueryUtil.bulid(queryDto));
        fillClassify(list);
        //记录搜索记录
        try {
            if(queryDto.getUserId() != null && CollUtil.isNotEmpty(queryDto.getRemark())) {
                for (String re : queryDto.getRemark()) {
                    Integer count = userSearchMapper.selectCount(Wrappers.<UserSearch>lambdaQuery()
                            .eq(UserSearch::getUserId, queryDto.getUserId())
                            .eq(UserSearch::getContent, re));
                    if(count <= 0) {
                        UserSearch us = new UserSearch();
                        us.setUserId(queryDto.getUserId());
                        us.setContent(re);
                        userSearchMapper.insert(us);
                    }
                }
            }
        } catch (Exception ex){
            log.error(StrUtil.format("{} 保存搜索记录失败:{}",queryDto.getUserId(), ex.getMessage()), ex);
        }
        return list;
    }

    @Override
    public Page queryPage(Page page, IncomeExpenseQueryDto queryDto) {
        Page<IncomeExpenseDto> pageResult = queryPage(page, QueryUtil.bulid(queryDto));
        fillClassify(pageResult.getRecord());
        //记录搜索记录
        try {
            if(queryDto.getUserId() != null && CollUtil.isNotEmpty(queryDto.getRemark())) {
                for (String re : queryDto.getRemark()) {
                    Integer count = userSearchMapper.selectCount(Wrappers.<UserSearch>lambdaQuery()
                            .eq(UserSearch::getUserId, queryDto.getUserId())
                            .eq(UserSearch::getContent, re));
                    if(count <= 0) {
                        UserSearch us = new UserSearch();
                        us.setUserId(queryDto.getUserId());
                        us.setContent(re);
                        userSearchMapper.insert(us);
                    }
                }
            }
        } catch (Exception ex){
            log.error(StrUtil.format("{} 保存搜索记录失败:{}",queryDto.getUserId(), ex.getMessage()), ex);
        }
        return pageResult;
    }

    @Override
    public SummaryDto querySummary(Long userId, Long accountBookId, int days) {
        SummaryDto summaryDto = new SummaryDto();
        //查询本月收支情况
        Date beginDate = DateUtil.beginOfMonth(new Date());
        Date endDate = DateUtil.endOfMonth(new Date());
        //收入支出
        List<Dict> sumAmount = baseMapper.querySumAmount(userId, accountBookId, beginDate, endDate, null, null, null);
        BigDecimal expense = BigDecimal.ZERO;
        BigDecimal income = BigDecimal.ZERO;
        for (Dict dict: sumAmount) {
            if(BillType.EXPENSE.getValue().equals(dict.getStr("type"))){
                expense = dict.getBigDecimal("amount");
            } else {
                income = dict.getBigDecimal("amount");
            }
        };
        //收支明细
        Date detail = DateUtil.beginOfDay(DateUtil.offsetDay(new Date(), -days));
        LambdaQueryWrapper<IncomeExpense> wrapper = Wrappers.<IncomeExpense>lambdaQuery()
                .eq(IncomeExpense::getUserId, userId)
                .ge(IncomeExpense::getCreateTime, detail)
                .orderByDesc(IncomeExpense::getDate,IncomeExpense::getCreateTime);
        if (accountBookId != null) {
            wrapper.eq(IncomeExpense::getAccountBookId, accountBookId);
        }
        List<IncomeExpenseDto> list = queryList(wrapper);
        fillClassify(list);
        //限额情况
        List<UserConfig> userConfigs = userConfigService.list(new LambdaQueryWrapper<UserConfig>().eq(UserConfig::getUserId, userId));
        UserConfig expenseShowType = userConfigs.stream().filter(userConfig -> SHOW_EXPENSE_LIMIT.equals(userConfig.getName())).findAny().orElseThrow(() -> new BusinessException("支出限额显示设置不存在"));
        BigDecimal expenseLimit = BigDecimal.ZERO;
        BigDecimal expenseSurplus = BigDecimal.ZERO;
        if(ExpenseLimitShowType.MONTHLY.getCode().equals(expenseShowType.getValue())){
            Optional<UserConfig> config = userConfigs.stream().filter(userConfig -> (EXPENSE_LIMIT_PREFIX + DateUtil.format(new Date(), DatePattern.SIMPLE_MONTH_PATTERN)).equals(userConfig.getName())).findAny();
            if(!config.isPresent()){
                config = userConfigs.stream().filter(userConfig -> DEFAULT_MONTHLY_EXPENSE_LIMIT.equals(userConfig.getName())).findAny();
                if(!config.isPresent()){
                    throw new BusinessException("默认月支出限额配置不存在");
                }
            }
            expenseLimit = new BigDecimal(config.get().getValue());
            expenseSurplus = expenseLimit.subtract(expense);
        } else if(ExpenseLimitShowType.YEARLY.getCode().equals(expenseShowType.getValue())){
            Optional<UserConfig> config = userConfigs.stream().filter(userConfig -> (EXPENSE_LIMIT_PREFIX + DateUtil.year(new Date())).equals(userConfig.getName())).findAny();
            if(!config.isPresent()){
                config = userConfigs.stream().filter(userConfig -> DEFAULT_YEARLY_EXPENSE_LIMIT.equals(userConfig.getName())).findAny();
                if(!config.isPresent()){
                    throw new BusinessException("默认年支出限额配置不存在");
                }
            }
            expenseLimit = new BigDecimal(config.get().getValue());
            //统计年支出
            List<Dict> sumYear = baseMapper.querySumAmount(userId, accountBookId, DateUtil.beginOfYear(new Date()), DateUtil.endOfYear(new Date()), null, null, null);
            Dict expenseYear = sumYear.stream().filter(dict -> BillType.EXPENSE.getValue().equals(dict.getStr("type"))).findAny().orElse(null);
            expenseSurplus = expenseYear == null ? expenseLimit : expenseLimit.subtract(expenseYear.getBigDecimal("amount"));
        }

        summaryDto.setExpenseAmount(expense);
        summaryDto.setIncomeAmount(income);
        summaryDto.setIncomeExpenseList(list);
        summaryDto.setExpenseLimit(expenseLimit);
        summaryDto.setExpenseSurplus(expenseSurplus);
        return summaryDto;
    }

    @Override
    public BillResultDto querySumAmountPeriod(BillQueryDto billQueryDto) {
        BillResultDto billResultDto = new BillResultDto();
        //确定开始结束时间
        Date beginDate = DateUtil.date(billQueryDto.getBeginDate());
        Date endDate = DateUtil.date(billQueryDto.getEndDate());

        //查询收入支出统计
        List<Dict> sumAmount = baseMapper.querySumAmount(billQueryDto.getUserId(), billQueryDto.getAccountBookId(), beginDate, endDate, billQueryDto.getClassifyList(), billQueryDto.getRemark(), billQueryDto.getTagCodes());
        BigDecimal expense = BigDecimal.ZERO;
        BigDecimal income = BigDecimal.ZERO;
        for (Dict dict: sumAmount) {
            if(BillType.EXPENSE.getValue().equals(dict.getStr("type"))){
                expense = dict.getBigDecimal("amount");
            } else {
                income = dict.getBigDecimal("amount");
            }
        };
        billResultDto.setIncomeTotal(income);
        billResultDto.setExpenseTotal(expense);

        //查询支出限额及剩余
        BigDecimal expenseLimit = BigDecimal.ZERO;
        BigDecimal expenseSurplus = BigDecimal.ZERO;
        UserConfig userConfig = userConfigService.getOne(new LambdaQueryWrapper<UserConfig>().eq(UserConfig::getUserId, billQueryDto.getUserId())
                .eq(UserConfig::getName, SHOW_EXPENSE_LIMIT));
        if(!ExpenseLimitShowType.NOT.getCode().equals(userConfig.getValue())){
            if(SUM_PERIOD_MONTH.equals(billQueryDto.getMode())){
                UserConfig uc = userConfigService.getOne(new LambdaQueryWrapper<UserConfig>().eq(UserConfig::getUserId, billQueryDto.getUserId())
                        .eq(UserConfig::getName, EXPENSE_LIMIT_PREFIX+DateUtil.format(beginDate, DatePattern.SIMPLE_MONTH_PATTERN)), false);
                if(uc == null){
                    uc = userConfigService.getOne(new LambdaQueryWrapper<UserConfig>().eq(UserConfig::getUserId, billQueryDto.getUserId())
                            .eq(UserConfig::getName, DEFAULT_MONTHLY_EXPENSE_LIMIT));
                }
                expenseLimit = new BigDecimal(uc.getValue());
            } else if(SUM_PERIOD_YEAR.equals(billQueryDto.getMode())){
                UserConfig uc = userConfigService.getOne(new LambdaQueryWrapper<UserConfig>().eq(UserConfig::getUserId, billQueryDto.getUserId())
                        .eq(UserConfig::getName, EXPENSE_LIMIT_PREFIX+DateUtil.year(beginDate)), false);
                if(uc == null){
                    uc = userConfigService.getOne(new LambdaQueryWrapper<UserConfig>().eq(UserConfig::getUserId, billQueryDto.getUserId())
                            .eq(UserConfig::getName, DEFAULT_YEARLY_EXPENSE_LIMIT));
                }
                expenseLimit = new BigDecimal(uc.getValue());
            }
            List<Dict> sumExpenseAmount = baseMapper.querySumAmount(billQueryDto.getUserId(), billQueryDto.getAccountBookId(), beginDate, endDate, null, null, null);
            Dict expenseSum = sumExpenseAmount.stream().filter(dict -> BillType.EXPENSE.getValue().equals(dict.getStr("type"))).findAny().orElse(null);
            expenseSurplus = expenseSum == null ? expenseLimit : expenseLimit.subtract(expenseSum.getBigDecimal("amount"));
        }
        billResultDto.setExpenseLimit(expenseLimit);
        billResultDto.setExpenseSurplus(expenseSurplus);
        //查询统计信息
        //账单
        Dict incomeExpenseSum = Dict.create();
        if(SUM_MODE_BILL.equals(billQueryDto.getQueryMode())){
            //构建一个全日期列表
            List<DateTime> dateTimes = DateUtil.rangeToList(beginDate, endDate, DateField.DAY_OF_MONTH);
            for (DateTime date: dateTimes) {
                Dict dict = Dict.create();
                String dateStr = SUM_PERIOD_YEAR.equals(billQueryDto.getMode()) ? date.toString(DatePattern.NORM_MONTH_PATTERN) : date.toString(DatePattern.NORM_DATE_PATTERN);
                dict.set("income", BigDecimal.ZERO).set("expense", BigDecimal.ZERO);
                incomeExpenseSum.set(dateStr, dict);
            }
            //查询数据
            List<Dict> datas;
            datas = SUM_PERIOD_YEAR.equals(billQueryDto.getMode()) ?
                    baseMapper.querySumAmountMonthly(billQueryDto.getUserId(), billQueryDto.getAccountBookId(), beginDate, endDate, billQueryDto.getClassifyList(), billQueryDto.getRemark(), billQueryDto.getTagCodes()) :
                    baseMapper.querySumAmountDaily(billQueryDto.getUserId(), billQueryDto.getAccountBookId(), beginDate, endDate, billQueryDto.getClassifyList(), billQueryDto.getRemark(), billQueryDto.getTagCodes());
            for (Dict data:datas) {
                Dict ies = (Dict) incomeExpenseSum.get(data.getStr("period"));
                if(BillType.INCOME.getValue().equals(data.getStr("type"))) {
                    ies.set("income", data.getBigDecimal("amount"));
                } else {
                    ies.set("expense", data.getBigDecimal("amount"));
                }
            }
        } else if(SUM_MODE_REPORT.equals(billQueryDto.getQueryMode())) {
            //报表
            List<Dict> datas = baseMapper.queryReportAmount(billQueryDto.getUserId(), billQueryDto.getAccountBookId(), beginDate, endDate, billQueryDto.getClassifyList(), billQueryDto.getRemark(), billQueryDto.getTagCodes());
            for(Dict data: datas) {
                Dict ies = (Dict) incomeExpenseSum.get(data.getStr("classify"));
                if(ies == null){
                    ies = Dict.create();
                }
                if(BillType.INCOME.getValue().equals(data.getStr("type"))) {
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
                .eq(billQueryDto.getAccountBookId() != null, IncomeExpense::getAccountBookId, billQueryDto.getAccountBookId())
                .le(IncomeExpense::getDate, endDate)
                .apply(billQueryDto.getClassifyList() != null && !billQueryDto.getClassifyList().isEmpty(),
                        "(" + buildClassifyCondition(billQueryDto.getClassifyList()) + ")")
                .like(billQueryDto.getRemark() != null && !billQueryDto.getRemark().isEmpty(), IncomeExpense::getRemark, billQueryDto.getRemark())
                .apply(billQueryDto.getTagCodes() != null && !billQueryDto.getTagCodes().isEmpty(),
                        "(" + buildTagCodesCondition(billQueryDto.getTagCodes()) + ")")
                .orderByDesc(IncomeExpense::getDate,IncomeExpense::getCreateTime));
        fillClassify(list);
        billResultDto.setIncomeExpenseList(list);
        return billResultDto;
    }

    /**
     * 填充单个收支记录的分类信息
     */
    private void fillClassifyForDto(IncomeExpenseDto dto) {
        if (dto.getMainClassify() != null) {
            ClassifyDto mclassify = classifyService.queryUserClassifyById(dto.getUserId(), dto.getMainClassify());
            if (mclassify != null) {
                dto.setMainClassifyName(mclassify.getName());
                dto.setMainClassifyImage(mclassify.getImage());
            }
        }
        if (dto.getSubClassify() != null) {
            ClassifyDto sclassify = classifyService.queryUserClassifyById(dto.getUserId(), dto.getSubClassify());
            if (sclassify != null) {
                dto.setSubClassifyName(sclassify.getName());
                dto.setSubClassifyImage(sclassify.getImage());
            }
        }
    }

    private void fillClassify(List<IncomeExpenseDto> list) {
        for (IncomeExpenseDto dto : list) {
            fillClassifyForDto(dto);
        }
    }

    @Override
    public IncomeExpenseDto queryById(Serializable id) {
        IncomeExpenseDto dto = baseMapstruct.toDto(getById(id));
        if (dto != null) {
            fillClassifyForDto(dto);
        }
        return dto;
    }

    @Override
    public IncomeExpenseDto create(IncomeExpenseDto dto) {
        if(dto.getIsAddRemark() == BoolEnum.YES && StringUtils.isNotBlank(dto.getRemark())){
            //remark添加到常用备注
            UserRemarkDto ur = new UserRemarkDto();
            ur.setUserId(dto.getUserId());
            ur.setRemark(dto.getRemark());
            ur.setClassifyId(dto.getMainClassify());
            userRemarkService.create(ur);
        }
        return super.create(dto);
    }

    @Override
    public boolean update(IncomeExpenseDto dto) {
        if(dto.getIsAddRemark() == BoolEnum.YES && StringUtils.isNotBlank(dto.getRemark())){
            //remark添加到常用备注
            UserRemarkDto ur = new UserRemarkDto();
            ur.setUserId(dto.getUserId());
            ur.setRemark(dto.getRemark());
            ur.setClassifyId(dto.getMainClassify());
            userRemarkService.create(ur);
        }
        return super.update(dto);
    }

    private String buildTagCodesCondition(List<Long> tagCodes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tagCodes.size(); i++) {
            if (i > 0) sb.append(" OR ");
            sb.append("FIND_IN_SET(").append(tagCodes.get(i)).append(", tag_codes) > 0");
        }
        return sb.toString();
    }

    private String buildClassifyCondition(List<ClassifyQueryItem> classifyList) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < classifyList.size(); i++) {
            if (i > 0) sb.append(" OR ");
            ClassifyQueryItem item = classifyList.get(i);
            if (item.getSubClassifyId() == null) {
                sb.append("main_classify = ").append(item.getMainClassifyId());
            } else {
                sb.append("main_classify = ").append(item.getMainClassifyId())
                        .append(" AND sub_classify = ").append(item.getSubClassifyId());
            }
        }
        return sb.toString();
    }

    @Override
    public void exportRecord(IncomeExpenseQueryDto queryDto, HttpServletResponse response) {
        Long userId = queryDto.getUserId();
        
        List<IncomeExpenseDto> list = queryList(QueryUtil.bulid(queryDto));
        
        Map<Long, AccountBook> accountBookMap = new HashMap<>();
        List<AccountBook> accountBooks = accountBookService.list(
                Wrappers.<AccountBook>lambdaQuery().eq(AccountBook::getUserId, userId)
        );
        accountBooks.forEach(ab -> accountBookMap.put(ab.getId(), ab));
        
        Map<Long, Classify> classifyMap = new HashMap<>();
        List<Classify> classifies = classifyService.list(
            Wrappers.<Classify>lambdaQuery().eq(Classify::getUserId, userId)
        );
        classifies.forEach(classify -> classifyMap.put(classify.getId(), classify));
        
        Map<String, UserTag> tagMap = new HashMap<>();
        if (list.stream().anyMatch(item -> item.getTagCodes() != null && !item.getTagCodes().isEmpty())) {
            List<UserTag> tagList = userTagService.list(
                Wrappers.<UserTag>lambdaQuery().eq(UserTag::getUserId, userId)
            );
            tagList.forEach(t -> tagMap.put(t.getCode().toString(), t));
        }
        
        List<ExportData> exportList = list.stream().map(item -> {
            ExportData data = new ExportData();
            data.setId(item.getId());
            data.setDate(item.getDate() != null ? item.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "");
            
            if (item.getAccountBookId() != null) {
                AccountBook accountBook = accountBookMap.get(item.getAccountBookId());
                data.setAccountBookName(accountBook != null ? accountBook.getName() : "");
            }
            
            data.setAmount(item.getAmount());
            data.setType(item.getType() != null ? item.getType().getName() : "");
            
            String classifyName = "";
            if (item.getMainClassify() != null) {
                Classify mainClassify = classifyMap.get(item.getMainClassify());
                if (mainClassify != null) {
                    if (item.getSubClassify() != null) {
                        Classify subClassify = classifyMap.get(item.getSubClassify());
                        classifyName = mainClassify.getName() + "/" + (subClassify != null ? subClassify.getName() : "");
                    } else {
                        classifyName = mainClassify.getName();
                    }
                }
            }
            data.setClassifyName(classifyName);
            
            data.setRemark(item.getRemark() != null ? item.getRemark() : "");
            data.setIsCreditCard(item.getIsCreditCard() != null ? item.getIsCreditCard().name() : "否");
            
            if (item.getTagCodes() != null && !item.getTagCodes().isEmpty()) {
                List<String> tagNames = Stream.of(item.getTagCodes().split(","))
                    .map(tagCode -> {
                        UserTag tag = tagMap.get(tagCode);
                        return tag != null ? tag.getName() : "";
                    })
                    .filter(name -> !name.isEmpty())
                    .collect(Collectors.toList());
                data.setTagNames(String.join(",", tagNames));
            }
            
            return data;
        }).collect(Collectors.toList());

        ExcelExportDto dto = new ExcelExportDto();
        dto.setSheetName("账单");

        List<ExcelExportDto.ColumnConfig<?>> columns = new ArrayList<>();

        ExcelExportDto.ColumnConfig<String> dateCol = new ExcelExportDto.ColumnConfig<>();
        dateCol.setHeader("日期");
        dateCol.setField("date");
        columns.add(dateCol);

        ExcelExportDto.ColumnConfig<String> accountBookCol = new ExcelExportDto.ColumnConfig<>();
        accountBookCol.setHeader("账本");
        accountBookCol.setField("accountBookName");
        columns.add(accountBookCol);

        ExcelExportDto.ColumnConfig<BigDecimal> amountCol = new ExcelExportDto.ColumnConfig<>();
        amountCol.setHeader("金额");
        amountCol.setField("amount");
        amountCol.setConverter(value -> value != null ? new DecimalFormat("0.00").format(value) : "0.00");
        columns.add(amountCol);

        ExcelExportDto.ColumnConfig<String> typeCol = new ExcelExportDto.ColumnConfig<>();
        typeCol.setHeader("类型");
        typeCol.setField("type");
        columns.add(typeCol);

        ExcelExportDto.ColumnConfig<String> classifyCol = new ExcelExportDto.ColumnConfig<>();
        classifyCol.setHeader("分类");
        classifyCol.setField("classifyName");
        columns.add(classifyCol);

        ExcelExportDto.ColumnConfig<String> remarkCol = new ExcelExportDto.ColumnConfig<>();
        remarkCol.setHeader("备注");
        remarkCol.setField("remark");
        columns.add(remarkCol);

        ExcelExportDto.ColumnConfig<String> creditCardCol = new ExcelExportDto.ColumnConfig<>();
        creditCardCol.setHeader("信用卡消费");
        creditCardCol.setField("isCreditCard");
        creditCardCol.setConverter(value -> "YES".equals(value) ? "是" : "否");
        columns.add(creditCardCol);

        ExcelExportDto.ColumnConfig<String> tagCol = new ExcelExportDto.ColumnConfig<>();
        tagCol.setHeader("标签");
        tagCol.setField("tagNames");
        tagCol.setConverter(value -> value == null ? "" : value);
        columns.add(tagCol);

        dto.setColumns(columns);
        dto.setData(exportList);

        try {
            ExcelExportUtils.export(response, dto);
        } catch (Exception e) {
            throw new BusinessException("导出失败: " + e.getMessage());
        }
    }

    @Data
    private static class ExportData {
        private Long id;
        private String date;
        private String accountBookName;
        private BigDecimal amount;
        private String type;
        private String classifyName;
        private String remark;
        private String isCreditCard;
        private String tagNames;
    }
}
