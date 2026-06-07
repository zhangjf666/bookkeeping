package com.hc.bookkeeping.modules.bkeeping.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hc.bookkeeping.common.base.BaseServiceImpl;
import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.common.utils.QueryUtil;
import com.hc.bookkeeping.modules.bkeeping.dto.ClassifyDto;
import com.hc.bookkeeping.modules.bkeeping.dto.ClassifyQueryDto;
import com.hc.bookkeeping.modules.bkeeping.entity.Classify;
import com.hc.bookkeeping.modules.bkeeping.mapper.ClassifyMapper;
import com.hc.bookkeeping.modules.bkeeping.mapstruct.ClassifyMapstruct;
import com.hc.bookkeeping.modules.bkeeping.service.ClassifyService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;

/**
 * <p>
 * 分类表 服务实现类
 * </p>
 *
 * @author zjf
 * @since 2021-07-27
 */
@Service
public class ClassifyServiceImpl extends BaseServiceImpl<ClassifyMapstruct, ClassifyDto, ClassifyMapper, Classify> implements ClassifyService {

    @Override
    public Page queryPage(ClassifyQueryDto queryDto, Page page) {
        return queryPage(page, QueryUtil.bulid(queryDto));
    }

    @Cacheable(value = "classify",key = "#userId +':'+ #classifyId",unless = "#result == null")
    @Override
    public ClassifyDto queryUserClassifyById(Long userId, Long classifyId) {
        Classify classify = baseMapper.selectOne(Wrappers.<Classify>lambdaQuery()
                .eq(Classify::getUserId, userId).eq(Classify::getId, classifyId));
        if(classify != null){
            return baseMapstruct.toDto(classify);
        }
        return null;
    }

    @CacheEvict(value = "classify", key = "#dto.userId +':'+ #dto.id")
    @Transactional(rollbackFor = Throwable.class)
    @Override
    public boolean update(ClassifyDto dto) {
        return baseMapper.updateById(baseMapstruct.toEntity(dto)) >= 1;
    }

    @CacheEvict(value = "classify", allEntries = true)
    @Transactional(rollbackFor = Throwable.class)
    @Override
    public boolean deleteByIds(Collection<? extends Serializable> ids) {
        for (Serializable id:ids) {
            baseMapper.deleteById(id);
            baseMapper.delete(new LambdaQueryWrapper<Classify>().eq(Classify::getPid, id));
        }
        return true;
    }
}
