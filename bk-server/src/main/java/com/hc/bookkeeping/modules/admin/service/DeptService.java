package com.hc.bookkeeping.modules.admin.service;

import com.hc.bookkeeping.common.base.BaseService;
import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.modules.admin.dto.DeptDto;
import com.hc.bookkeeping.modules.admin.dto.DeptQueryDto;
import com.hc.bookkeeping.modules.admin.dto.vo.DeptVo;
import com.hc.bookkeeping.modules.admin.entity.Dept;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * <p>
 * 部门表 服务类
 * </p>
 *
 * @author zjf
 * @since 2020-06-11
 */
public interface DeptService extends BaseService<DeptDto, Dept> {

    /**
     * 分页查询
     * @param queryDto 查询条件
     * @param page 分页
     * @return
     */
    Page queryPage(DeptQueryDto queryDto, Page page);

    /**
     * 查询
     * @param queryDto 查询条件
     * @return
     */
    List<DeptDto> query(DeptQueryDto queryDto);

    /**
     * 根据角色id获取,角色对应的权限部门
     * @param roleId 角色id
     * @return
     */
    Set<DeptDto> getDeptsByRoleId(@Param("roleId") Set<Long> roleId);

    /**
     * 获取指定节点的所有上级节点
     * @param dto 指定节点
     * @param depts 缓存节点列表
     * @return
     */
    List<DeptDto> getSuperior(DeptDto dto, List<Dept> depts);

    /**
     * 判断是否有子节点
     * @param trees
     */
    void superiorCheckChildren(List<DeptVo> trees);
}
