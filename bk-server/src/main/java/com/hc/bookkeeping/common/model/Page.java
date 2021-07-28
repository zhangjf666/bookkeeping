package com.hc.bookkeeping.common.model;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/6/23 9:15
 */
@Getter
@Setter
public class Page<T> implements Serializable {
    private static int firstPageNo = 1;

    public Page() {}

    public Page(int pageNum, int pageSize) {
        this.pageNo = pageNum;
        this.pageSize = pageSize;
    }

    /**
     * 当前页码
     */
    private int pageNo = 1;
    /**
     * 每页记录数
     */
    private int pageSize = 20;
    /**
     * 总数据量
     */
    private long totalCount;
    /**
     * 总页数
     */
    private long totalPages;
    /**
     * 数据
     */
    private List<T> record;

    public void setTotalCount(long totalCount){
        this.totalCount = totalCount;
        this.totalPages = computeTotalPages(this.totalCount);
    }

    public void setRecordForTotalRecord(List<T> totalRecord){
        this.totalCount = totalRecord.size();
        this.totalPages = computeTotalPages(this.totalCount);
        this.record = totalRecord.subList(getStart(),totalRecord.size() < getEnd() ? totalRecord.size() : getEnd());
    }

    private int getStart() {
        if (pageNo < firstPageNo) {
            pageNo = firstPageNo;
        }

        if (pageSize < 1) {
            pageSize = 0;
        }

        return (pageNo - firstPageNo) * pageSize;
    }

    private int getEnd() {
        int start = getStart();
        return getEndByStart(start);
    }

    private int getEndByStart(int start) {
        if (pageSize < 1) {
            pageSize = 0;
        }

        return start + pageSize;
    }

    private long computeTotalPages(long totalCount){
        if (pageSize == 0) {
            return 0;
        } else {
            return totalCount % pageSize == 0 ? totalCount / pageSize : totalCount / pageSize + 1;
        }
    }

    public static<T> Page<T> toPage(int pageNum, int pageSize, List<T> totalRecord){
        Page<T> page = new Page<T>(pageNum,pageSize);
        page.setRecordForTotalRecord(totalRecord);
        return page;
    }

    public static<T> Page<T> toPage(List<T> totalRecord){
        Page<T> page = new Page<T>();
        page.setRecordForTotalRecord(totalRecord);
        return page;
    }

    public static<T> Page<T> fromPageable(Pageable pageable){
        Page<T> page = new Page<T>();
        page.setPageNo(pageable.getPageNumber());
        page.setPageSize(pageable.getPageSize());
        return page;
    }

    public static<T> Page<T> fromMybatisPlusPage(IPage<T> mPage){
        Page<T> page = new Page<T>();
        page.setPageNo((int) mPage.getCurrent());
        page.setPageSize((int) mPage.getSize());
        page.setRecord(mPage.getRecords());
        page.setTotalCount(mPage.getTotal());
        page.setTotalPages(mPage.getPages());
        return page;
    }

    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<T> toMybatisPlusPage(){
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<T> mPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>();
        mPage.setCurrent(this.pageNo);
        mPage.setSize(this.pageSize);
        return mPage;
    }
}
