package com.hc.bookkeeping.common.model;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.hc.bookkeeping.common.base.IEnum;

public enum BoolEnum implements IEnum<String> {
    /**
     * 否
     */
    NO("0", "否"),
    /**
     * 是
     */
    YES("1", "是");

    @EnumValue
    private final String type;

    private final String name;

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

    @Override
    public String getValue() {
        return type;
    }

    public static BoolEnum findEmum(String id){
        for (BoolEnum item : BoolEnum.values()) {
            if (item.getValue().equals(id)) {
                return item;
            }
        }
        return null;
    }
}
