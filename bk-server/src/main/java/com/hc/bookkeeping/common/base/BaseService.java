package com.hc.bookkeeping.common.base;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hc.bookkeeping.common.model.Page;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/8/17 14:45
 */
public interface BaseService<D,E> extends IService<E> {

    BaseMapstruct<D,E> getBaseMapstruct();

    List<D> queryList(Wrapper<E> wrapper);

    Page<D> queryPage(Page page, Wrapper<E> wrapper);

    D queryOne(Wrapper<E> wrapper);

    D queryById(Serializable id);

    /**
     * 保存
     * @param dto Dto
     * @return
     */
    D create(D dto);

    /**
     * 更新
     * @param dto Dto
     * @return
     */
    boolean update(D dto);

    /**
     * 批量删除
     * @param ids id列表
     * @return
     */
    boolean delete(Collection<? extends Serializable> ids);
}
