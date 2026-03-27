package com.hc.bookkeeping.modules.bkeeping.dto;

import com.hc.bookkeeping.modules.bkeeping.constants.ExpenseLimitShowType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class AdditionalExpenseLimitDto {

    /**
     * 用户id
     */
    @ApiModelProperty(value = "用户id")
    private Long userId;

    /**
     * 支出限额类型
     */
    @ApiModelProperty(value = "支出限额类型")
    private ExpenseLimitShowType type;

    /**
     * 支出限额值
     */
    @ApiModelProperty(value = "支出限额值")
    private String expenseLimit;
}
