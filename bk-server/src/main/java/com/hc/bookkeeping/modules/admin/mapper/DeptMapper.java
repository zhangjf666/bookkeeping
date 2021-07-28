package com.hc.bookkeeping.modules.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hc.bookkeeping.modules.admin.entity.Dept;
import org.apache.ibatis.annotations.Param;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * <p>
 * 部门表 Mapper 接口
 * </p>
 *
 * @author zjf
 * @since 2020-06-11
 */
public interface DeptMapper extends BaseMapper<Dept> {

    /**
     * 根据角色id获取,角色对应的部门
     * @param roleId 角色id
     * @return
     */
    LinkedHashSet<Dept> getDeptsByRoleId(@Param("roleId") Set<Long> roleId);
}
