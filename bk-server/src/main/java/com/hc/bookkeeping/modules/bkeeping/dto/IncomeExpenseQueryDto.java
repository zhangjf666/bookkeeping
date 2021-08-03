package com.hc.bookkeeping.modules.bkeeping.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.hc.bookkeeping.common.annotation.Query;
import com.hc.bookkeeping.common.annotation.Sort;
import com.hc.bookkeeping.common.model.BoolEnum;
import com.hc.bookkeeping.modules.bkeeping.model.BillType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 收入支出表
 * </p>
 *
 * @author zjf
 * @since 2021-07-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="IncomeExpense对象", description="收入支出表")
public class IncomeExpenseQueryDto implements Serializable {

    @ApiModelProperty(value = "id")
    @Query
    private Long id;

    @ApiModelProperty(value = "用户id")
    @Query
    private Long userId;

    @ApiModelProperty(value = "账本id")
    @Query
    private Long accountBookId;

    @ApiModelProperty(value = "金额")
    @Query(match = Query.Matching.BETWEEN)
    private List<BigDecimal> amount;

    @ApiModelProperty(value = "类型(0:支出,1:收入)")
    @Query
    private BillType type;

    @ApiModelProperty(value = "产生时间")
    @Query(match = Query.Matching.BETWEEN)
    @Sort(sort = Sort.SortType.DESC)
    private List<LocalDate> date;

    @ApiModelProperty(value = "备注")
    @Query(match = Query.Matching.INNER_LIKE)
    private String remark;

    @ApiModelProperty(value = "主分类id")
    @Query
    private Long mainClassify;

    @ApiModelProperty(value = "子分类id")
    @Query
    private Long subClassify;

    @ApiModelProperty(value = "是否信用卡消费(0:否,1;是)")
    @Query
    private BoolEnum isCreditCard;
}
