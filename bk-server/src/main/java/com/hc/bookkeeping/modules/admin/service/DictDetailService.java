package com.hc.bookkeeping.modules.admin.service;

import com.hc.bookkeeping.common.base.BaseService;
import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.modules.admin.dto.DictDetailDto;
import com.hc.bookkeeping.modules.admin.dto.DictDetailQueryDto;
import com.hc.bookkeeping.modules.admin.dto.vo.DictDetailVo;
import com.hc.bookkeeping.modules.admin.entity.DictDetail;

import java.util.List;

/**
 * <p>
 * 字典明细表 服务类
 * </p>
 *
 * @author zjf
 * @since 2020-06-11
 */
public interface DictDetailService extends BaseService<DictDetailDto, DictDetail> {

    /**
     * 分页查询
     * @param queryDto 查询条件
     * @param page 分页
     * @return
     */
    Page queryPage(DictDetailQueryDto queryDto, Page page);

    /**
     * 查询
     * @param queryDto 查询条件
     * @return
     */
    List<DictDetailDto> query(DictDetailQueryDto queryDto);

    /**
     * 获取指定节点的所有上级节点
     * @param dto 指定节点
     * @param menus 缓存节点列表
     * @return
     */
    List<DictDetailDto> getSuperior(DictDetailDto dto, List<DictDetail> menus);

    /**
     * 判断是否有子节点
     * @param trees
     */
    void superiorCheckChildren(List<DictDetailVo> trees);
}
