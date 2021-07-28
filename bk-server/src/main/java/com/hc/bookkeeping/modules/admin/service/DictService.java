package com.hc.bookkeeping.modules.admin.service;

import com.hc.bookkeeping.common.base.BaseService;
import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.modules.admin.dto.DictDto;
import com.hc.bookkeeping.modules.admin.dto.DictQueryDto;
import com.hc.bookkeeping.modules.admin.entity.Dict;

import java.util.List;
import java.util.Set;

/**
 * <p>
 * 数据字典表 服务类
 * </p>
 *
 * @author zjf
 * @since 2020-06-11
 */
public interface DictService extends BaseService<DictDto, Dict> {

    /**
     * 分页查询
     * @param queryDto 查询条件
     * @param page 分页
     * @return
     */
    Page queryPage(DictQueryDto queryDto, Page page);

    /**
     * 查询
     * @param queryDto 查询条件
     * @return
     */
    List<DictDto> query(DictQueryDto queryDto);

    /**
     * 删除字典
     * @param ids 字典id
     * @return
     */
    boolean deleteDicts(Set<Long> ids);
}
