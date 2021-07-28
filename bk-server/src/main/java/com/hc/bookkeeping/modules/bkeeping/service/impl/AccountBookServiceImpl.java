package com.hc.bookkeeping.modules.bkeeping.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hc.bookkeeping.common.base.BaseServiceImpl;
import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.common.utils.QueryUtil;
import com.hc.bookkeeping.modules.admin.dto.DictDetailDto;
import com.hc.bookkeeping.modules.admin.dto.DictDetailQueryDto;
import com.hc.bookkeeping.modules.bkeeping.dto.AccountBookDto;
import com.hc.bookkeeping.modules.bkeeping.dto.AccountBookQueryDto;
import com.hc.bookkeeping.modules.bkeeping.entity.AccountBook;
import com.hc.bookkeeping.modules.bkeeping.mapper.AccountBookMapper;
import com.hc.bookkeeping.modules.bkeeping.mapstruct.AccountBookMapstruct;
import com.hc.bookkeeping.modules.bkeeping.service.AccountBookService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * <p>
 * 账本 服务实现类
 * </p>
 *
 * @author zjf
 * @since 2021-07-27
 */
@Service
public class AccountBookServiceImpl extends BaseServiceImpl<AccountBookMapstruct, AccountBookDto, AccountBookMapper, AccountBook> implements AccountBookService {

    @Override
    public Page<AccountBookDto> queryPage(AccountBookQueryDto queryDto, Page page) {
        return queryPage(page, QueryUtil.bulid(queryDto));
    }
}
