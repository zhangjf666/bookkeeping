package com.hc.bookkeeping.modules.bkeeping.model;

import lombok.Data;

import java.util.List;

@Data
public class UserDefaultConfig {
    private List<AccountBookConfig> accountBook;
    private List<UserConfigItem> userConfig;
    private List<ClassifyConfig> userClassify;
    
    @Data
    public static class AccountBookConfig {
        private String name;
        private String image;
        private String description;
        private String isDefault;
    }
    
    @Data
    public static class UserConfigItem {
        private String name;
        private String value;
        private String description;
    }
    
    @Data
    public static class ClassifyConfig {
        private String name;
        private String image;
        private Integer sort;
        private String type;
        private String enable;
        private List<ClassifyConfig> children;
    }
}