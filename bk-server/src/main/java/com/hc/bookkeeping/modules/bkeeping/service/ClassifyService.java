package com.hc.bookkeeping.modules.bkeeping.service;

import com.hc.bookkeeping.common.base.BaseService;
import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.modules.bkeeping.dto.ClassifyDto;
import com.hc.bookkeeping.modules.bkeeping.dto.ClassifyQueryDto;
import com.hc.bookkeeping.modules.bkeeping.entity.Classify;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 分类表 服务类
 * </p>
 *
 * @author zjf
 * @since 2021-07-27
 */
public interface ClassifyService extends BaseService<ClassifyDto, Classify> {

    /**
     * 分页查询
     * @param queryDto 查询条件
     * @param page 分页
     * @return
     */
    Page queryPage(ClassifyQueryDto queryDto, Page page);
}
