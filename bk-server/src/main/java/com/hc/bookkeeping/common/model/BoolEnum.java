package com.hc.bookkeeping.common.model;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.hc.bookkeeping.common.base.IEnum;
import com.hc.bookkeeping.modules.bkeeping.model.BillType;

public enum BoolEnum implements IEnum<String> {
    /**
     * 否
     */
    False("0", "否"),
    /**
     * 是
     */
    True("1", "是");

    @EnumValue
    private String type;

    private String name;

    BoolEnum(String type, String name){
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
    public static BoolEnum findEmum(String id){
        for (BoolEnum item : BoolEnum.values()) {
            if (item.getValue().equals(id)) {
                return item;
            }
        }
        return null;
    }
}
