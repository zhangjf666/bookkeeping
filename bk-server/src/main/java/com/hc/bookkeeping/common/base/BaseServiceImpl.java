package com.hc.bookkeeping.common.base;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.common.utils.QueryUtil;
import com.hc.bookkeeping.modules.bkeeping.entity.AccountBook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/8/17 14:49
 */
public class BaseServiceImpl<S extends BaseMapstruct<D,E>,D,M extends BaseMapper<E>,E> extends ServiceImpl<M,E>
        implements BaseService<D,E> {

    @Autowired
    protected S baseMapstruct;

    @Override
    public BaseMapstruct<D, E> getBaseMapstruct() {
        return this.baseMapstruct;
    }

    @Override
    public List<D> queryList(Wrapper<E> wrapper) {
        List<E> list = list(wrapper);
        return baseMapstruct.toDto(list);
    }

    @Override
    public Page<D> queryPage(Page page, Wrapper<E> wrapper) {
        Page rpage = Page.fromMybatisPlusPage(baseMapper.selectPage(page.toMybatisPlusPage(), wrapper));
        rpage.setRecord(baseMapstruct.toDto(rpage.getRecord()));
        return rpage;
    }

    @Override
    public D queryOne(Wrapper<E> wrapper) {
        E entity = getOne(wrapper);
        return baseMapstruct.toDto(entity);
    }

    @Override
    public D queryById(Serializable id) {
        return baseMapstruct.toDto(getById(id));
    }

    @Override
    public D create(D dto) {
        E e = baseMapstruct.toEntity(dto);
        baseMapper.insert(e);
        return baseMapstruct.toDto(e);
    }

    @Override
    public boolean update(D dto) {
        return baseMapper.updateById(baseMapstruct.toEntity(dto)) >= 1;
    }

    @Transactional(rollbackFor = Throwable.class)
    @Override
    public boolean delete(Collection<? extends Serializable> ids) {
        return baseMapper.deleteBatchIds(ids) > 0;
    }
}
