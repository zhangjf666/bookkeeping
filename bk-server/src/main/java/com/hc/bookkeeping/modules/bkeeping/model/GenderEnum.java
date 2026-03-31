package com.hc.bookkeeping.modules.bkeeping.model;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 性别
 */
@Getter
public enum GenderEnum {

    MALE("1", "男性"),
    FEMALE("2", "女性");

    @EnumValue
    private final String code;
    private final String desc;

    GenderEnum(String code, String desc){
        this.code = code;
        this.desc = desc;
    }
}
