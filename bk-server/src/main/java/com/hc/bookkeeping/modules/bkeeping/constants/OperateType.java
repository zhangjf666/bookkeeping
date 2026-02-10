package com.hc.bookkeeping.modules.bkeeping.constants;

/**
 * 操作类型
 */
public enum OperateType {

    EXPENSE("0", "支出"),
    INCOME("1", "收入");

    public final String code;
    public final String desc;

    private OperateType(String code, String desc){
        this.code = code;
        this.desc = desc;
    }
}
