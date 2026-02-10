package com.hc.bookkeeping.modules.bkeeping.dto;

import lombok.Data;

@Data
public class AdditionalExpenseLimitDto {

    private Long userId;
    private String type;
    private String expenseLimit;
}
