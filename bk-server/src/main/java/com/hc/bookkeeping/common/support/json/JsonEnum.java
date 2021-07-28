package com.hc.bookkeeping.common.support.json;

public enum JsonEnum {
    FAST_JSON("fastjson"),
    GSON("gson"),
    JACKSON("jackson");
    private String classType;

    JsonEnum() {
    }

    JsonEnum(String classType) {
        this.classType = classType;
    }
    public String getClassType() {
        return classType;
    }

    public void setClassType(String classType) {
        this.classType = classType;
    }

    public static JsonEnum getJsonMappeEnum(String jsonName) {
        for (JsonEnum item : JsonEnum.values()) {
            if (jsonName.equals(FAST_JSON.classType)) {
                return FAST_JSON;
            }
            if (jsonName.equals(GSON.getClassType())) {
                return GSON;
            }
            if (jsonName.equals(JACKSON.classType)) {
                return JACKSON;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
