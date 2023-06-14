package com.hc.bookkeeping.modules.bkeeping.dto;

import com.hc.bookkeeping.common.model.BoolEnum;
import com.hc.bookkeeping.modules.bkeeping.model.BillType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
public class IncomeExpenseDto implements Serializable {

    @ApiModelProperty(value = "id")
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
    private BoolEnum isCreditCard;

    @ApiModelProperty(value = "是否加入常用备注(0:否,1;是)")
    private BoolEnum addRemark;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "主分类名称")
    private String mainClassifyName;

    @ApiModelProperty(value = "子分类名称")
    private String subClassifyName;

    @ApiModelProperty(value = "主分类图标")
    private String mainClassifyImage;

    @ApiModelProperty(value = "子分类图标")
    private String subClassifyImage;
}
