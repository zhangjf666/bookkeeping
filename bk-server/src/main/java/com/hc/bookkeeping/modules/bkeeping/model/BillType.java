package com.hc.bookkeeping.modules.bkeeping.model;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.hc.bookkeeping.common.base.IEnum;

public enum BillType implements IEnum<String> {
    /**
     * 支出
     */
    Expense("0", "支出"),
    /**
     * 收入
     */
    Income("1", "收入");

    @EnumValue
    private String type;

    private String name;

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

    @JsonValue
    @Override
    public String getValue() {
        return type;
    }

    @JsonCreator
    public static BillType findEmum(String id){
        for (BillType item : BillType.values()) {
            if (item.getValue().equals(id)) {
                return item;
            }
        }
        return null;
    }
}
