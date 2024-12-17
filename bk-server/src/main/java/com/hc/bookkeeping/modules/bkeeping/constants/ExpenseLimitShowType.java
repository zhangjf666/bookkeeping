package com.hc.bookkeeping.modules.bkeeping.constants;

/**
 * 支出限额显示类型
 */
public enum ExpenseLimitShowType {

    NOT_SHOW("1", "不显示"),
    MONTHLY_SHOW("2", "显示月限额"),
    YEARLY_SHOW("3", "显示年限额");

    public final String code;
    public final String desc;

    private ExpenseLimitShowType(String code, String desc){
        this.code = code;
        this.desc = desc;
    }
}
