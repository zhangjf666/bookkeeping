package com.hc.bookkeeping.common.model;

import com.hc.bookkeeping.common.annotation.Sort;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/7/7 11:04
 */
@Data
@AllArgsConstructor
public class SortItem implements Serializable, Comparable<SortItem> {

    private int order;

    private String column;

    private Sort.SortType type;

    @Override
    public int compareTo(SortItem o) {
        return this.order - o.order;
    }
}
