package com.hc.bookkeeping.modules.bkeeping.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.hc.bookkeeping.common.model.BoolEnum;
import com.hc.bookkeeping.modules.bkeeping.model.BillType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
@TableName("income_expense")
@ApiModel(value="IncomeExpense对象", description="收入支出表")
public class IncomeExpense implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "账本id")
    private Long accountBookId;

    @ApiModelProperty(value = "金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "类型(0:支出,1:收入)")
    private BillType type;

    @ApiModelProperty(value = "产生时间")
    private LocalDate date;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "主分类id")
    private Long mainClassify;

    @ApiModelProperty(value = "子分类id")
    private Long subClassify;

    @ApiModelProperty(value = "是否信用卡消费(0:否,1;是)")
    @TableField("is_credit_card")
    private BoolEnum isCreditCard;

    @ApiModelProperty(value = "所属标签id列表")
    @TableField("tag_codes")
    private String tagCodes;

    @ApiModelProperty(value = "创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
