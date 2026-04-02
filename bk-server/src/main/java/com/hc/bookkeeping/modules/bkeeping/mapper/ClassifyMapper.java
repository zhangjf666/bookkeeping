package com.hc.bookkeeping.modules.bkeeping.mapper;

import com.hc.bookkeeping.modules.bkeeping.entity.Classify;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 分类表 Mapper 接口
 * </p>
 *
 * @author zjf
 * @since 2021-07-27
 */
@Mapper
public interface ClassifyMapper extends BaseMapper<Classify> {

    @Select("SELECT id, name, pid, image, sort, type, enable FROM classify WHERE user_id = -1 ORDER BY sort")
    List<Classify> selectDefaultClassifies();

    void batchInsert(@Param("list") List<Classify> classifies);
}
