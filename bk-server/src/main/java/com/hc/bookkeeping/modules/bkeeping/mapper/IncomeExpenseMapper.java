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
     * @param userId 用户id
     * @param accountBookId 账本id（可选）
     * @param beginDate 开始日期
     * @param endDate 结束日期
     * @param classify 分类列表
     * @param remark 备注（模糊查询）
     * @param tagCodes 标签codes（多选，包含任意一个即可）
     * @return
     */
    List<Dict> querySumAmount(Long userId, Long accountBookId, Date beginDate, Date endDate, List<Long> classify, String remark, List<Long> tagCodes);

    /**
     * 按月统计总收入支出
     * @param userId 用户id
     * @param accountBookId 账本id（可选）
     * @param beginDate 开始日期
     * @param endDate 结束日期
     * @param classify 分类列表
     * @param remark 备注（模糊查询）
     * @param tagCodes 标签codes（多选，包含任意一个即可）
     * @return
     */
    List<Dict> querySumAmountMonthly(Long userId, Long accountBookId, Date beginDate, Date endDate, List<Long> classify, String remark, List<Long> tagCodes);

    /**
     * 按日统计总收入支出
     * @param userId 用户id
     * @param accountBookId 账本id（可选）
     * @param beginDate 开始日期
     * @param endDate 结束日期
     * @param classify 分类列表
     * @param remark 备注（模糊查询）
     * @param tagCodes 标签codes（多选，包含任意一个即可）
     * @return
     */
    List<Dict> querySumAmountDaily(Long userId, Long accountBookId, Date beginDate, Date endDate, List<Long> classify, String remark, List<Long> tagCodes);

    /**
     * 按分类统计总收入支出
     * @param userId 用户id
     * @param accountBookId 账本id（可选）
     * @param beginDate 开始日期
     * @param endDate 结束日期
     * @param classify 分类列表
     * @param remark 备注（模糊查询）
     * @param tagCodes 标签codes（多选，包含任意一个即可）
     * @return
     */
    List<Dict> queryReportAmount(Long userId, Long accountBookId, Date beginDate, Date endDate, List<Long> classify, String remark, List<Long> tagCodes);
}
