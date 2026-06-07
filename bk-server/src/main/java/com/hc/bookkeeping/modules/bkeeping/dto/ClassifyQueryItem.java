package com.hc.bookkeeping.modules.bkeeping.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 分类查询项，包含主分类和子分类
 */
@Data
public class ClassifyQueryItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主分类ID（顶级分类）
     */
    private Long mainClassifyId;

    /**
     * 子分类ID（可以为null）
     */
    private Long subClassifyId;
}