package com.hc.bookkeeping.modules.bkeeping.model;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.hc.bookkeeping.common.base.IEnum;

public enum BillType implements IEnum<String> {
    /**
     * 支出
     */
    EXPENSE("0", "支出"),
    /**
     * 收入
     */
    INCOME("1", "收入");

    @EnumValue
    private final String type;

    private final String name;

    BillType(String type, String name){
        this.type = type;
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

    @Override
    public String toString(){
        return type;
    }

    @Override
    public String getValue() {
        return type;
    }

    public static BillType findEnum(String id){
        for (BillType item : BillType.values()) {
            if (item.getValue().equals(id)) {
                return item;
            }
        }
        return null;
    }
}
