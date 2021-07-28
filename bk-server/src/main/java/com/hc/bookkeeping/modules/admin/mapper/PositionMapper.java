package com.hc.bookkeeping.modules.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hc.bookkeeping.modules.admin.entity.Position;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Set;

/**
 * <p>
 * 职位表 Mapper 接口
 * </p>
 *
 * @author zjf
 * @since 2020-06-11
 */
public interface PositionMapper extends BaseMapper<Position> {

    /**
     * 查询用户岗位
     * @param userId 用户id
     * @return 岗位
     */
    @Select("select * from sys_position where id in (select position_id from sys_user_position where user_id = #{userId})")
    Set<Position> getUserPosition(Long userId);

    /**
     * 用户id查询用户角色
     * @param userId 用户id
     * @return 角色集合
     */
    Set<Position> getPositionByUserId(@Param("userId") Long userId);
}
