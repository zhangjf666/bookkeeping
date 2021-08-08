package com.hc.bookkeeping.modules.bkeeping.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 账单跟报表查询实体
 */
@Data
public class BillQueryDto {

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 查询方式(0:账单,1:报表)
     */
    private String queryMode;

    /**
     * 查询模式(0:月,1:年,2:自定义)
     */
    private String mode;

    /**
     * 开始时间
     */
    private LocalDate beginDate;

    /**
     * 结束时间
     */
    private LocalDate endDate;

    /**
     * 查询的分类
     */
    private List<Long> classifyList = new ArrayList<>();
}
