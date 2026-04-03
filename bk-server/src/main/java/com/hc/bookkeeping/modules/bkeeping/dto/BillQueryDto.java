package com.hc.bookkeeping.modules.bkeeping.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 账单跟报表查询实体
 */
@Data
public class BillQueryDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 账本id
     */
    private Long accountBookId;

    /**
     * 查询方式(0:账单,1:报表)
     */
    private String queryMode;

    /**
     * 查询模式(0:月(按天展示),1:年(按月展示))
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
    private List<ClassifyQueryItem> classifyList = new ArrayList<>();

    /**
     * 备注（模糊查询）
     */
    private String remark;

    /**
     * 标签codes（多选，包含任意一个即可）
     */
    private List<Long> tagCodes = new ArrayList<>();
}
