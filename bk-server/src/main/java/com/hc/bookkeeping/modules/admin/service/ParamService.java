package com.hc.bookkeeping.modules.admin.service;

import com.hc.bookkeeping.common.base.BaseService;
import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.modules.admin.dto.ParamDto;
import com.hc.bookkeeping.modules.admin.dto.ParamQueryDto;
import com.hc.bookkeeping.modules.admin.entity.Param;

import java.util.List;

/**
 * <p>
 * 系统参数表 服务类
 * </p>
 *
 * @author zjf
 * @since 2020-06-11
 */
public interface ParamService extends BaseService<ParamDto, Param> {

    /**
     * 分页查询
     * @param queryDto 查询条件
     * @param page 分页
     * @return
     */
    Page queryPage(ParamQueryDto queryDto, Page page);

    /**
     * 查询
     * @param queryDto 查询条件
     * @return
     */
    List<ParamDto> query(ParamQueryDto queryDto);
}
