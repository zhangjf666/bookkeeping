package com.hc.bookkeeping.modules.bkeeping.service.impl;

import com.hc.bookkeeping.common.base.BaseServiceImpl;
import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.common.utils.QueryUtil;
import com.hc.bookkeeping.modules.bkeeping.dto.ClassifyDto;
import com.hc.bookkeeping.modules.bkeeping.dto.ClassifyQueryDto;
import com.hc.bookkeeping.modules.bkeeping.entity.Classify;
import com.hc.bookkeeping.modules.bkeeping.mapper.ClassifyMapper;
import com.hc.bookkeeping.modules.bkeeping.mapstruct.ClassifyMapstruct;
import com.hc.bookkeeping.modules.bkeeping.service.ClassifyService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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
}
