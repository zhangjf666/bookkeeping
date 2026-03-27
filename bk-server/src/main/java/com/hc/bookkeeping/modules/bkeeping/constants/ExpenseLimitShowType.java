package com.hc.bookkeeping.modules.bkeeping.constants;

import lombok.Getter;

/**
 * 支出限额显示类型
 */
@Getter
public enum ExpenseLimitShowType {

    NOT("1", "不显示"),
    MONTHLY("2", "显示月限额"),
    YEARLY("3", "显示年限额");

    private final String code;
    private final String desc;

    ExpenseLimitShowType(String code, String desc){
        this.code = code;
        this.desc = desc;
    }
}
