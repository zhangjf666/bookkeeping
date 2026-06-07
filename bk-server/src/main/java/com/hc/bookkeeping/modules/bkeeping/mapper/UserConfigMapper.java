package com.hc.bookkeeping.modules.bkeeping.mapper;

import com.hc.bookkeeping.modules.bkeeping.entity.UserConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 用户配置表 Mapper 接口
 * </p>
 *
 * @author zjf
 * @since 2021-07-27
 */
@Mapper
public interface UserConfigMapper extends BaseMapper<UserConfig> {

    @Select("SELECT id, user_id, name, value, description, enable FROM user_config WHERE user_id = -1")
    List<UserConfig> selectDefaultConfigs();

    void batchInsert(@Param("list") List<UserConfig> configs);
}
