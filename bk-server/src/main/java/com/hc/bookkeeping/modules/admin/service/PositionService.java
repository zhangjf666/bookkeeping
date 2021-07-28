package com.hc.bookkeeping.modules.admin.service;

import com.hc.bookkeeping.common.base.BaseService;
import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.modules.admin.dto.PositionDto;
import com.hc.bookkeeping.modules.admin.dto.PositionQueryDto;
import com.hc.bookkeeping.modules.admin.entity.Position;

import java.util.List;
import java.util.Set;

/**
 * <p>
 * 职位表 服务类
 * </p>
 *
 * @author zjf
 * @since 2020-06-11
 */
public interface PositionService extends BaseService<PositionDto, Position> {

    /**
     * 分页查询
     * @param queryDto 查询条件
     * @param page 分页
     * @return
     */
    Page queryPage(PositionQueryDto queryDto, Page page);

    /**
     * 查询
     * @param queryDto 查询条件
     * @return
     */
    List<PositionDto> query(PositionQueryDto queryDto);

    /**
     * 获取用户对应的岗位
     * @param userId 用户id
     * @return
     */
    Set<PositionDto> getPositionByUserId(Long userId);
}
