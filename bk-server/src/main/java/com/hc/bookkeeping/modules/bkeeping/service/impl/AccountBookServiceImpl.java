package com.hc.bookkeeping.modules.bkeeping.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hc.bookkeeping.common.base.BaseServiceImpl;
import com.hc.bookkeeping.common.exception.BusinessException;
import com.hc.bookkeeping.common.model.BoolEnum;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AccountBookDto create(AccountBookDto dto) {
        if(dto.getIsDefault() == BoolEnum.YES){
            update(new LambdaUpdateWrapper<AccountBook>().set(AccountBook::getIsDefault, BoolEnum.NO).eq(AccountBook::getUserId, dto.getUserId()));
        }
        return super.create(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(AccountBookDto dto) {
        //如果本来是就是默认账本，无法设置为非默认
        AccountBook accountBook = getById(dto.getId());
        if(accountBook == null){
            return true;
        }
        if(accountBook.getIsDefault() == BoolEnum.YES && dto.getIsDefault() == BoolEnum.NO){
            throw new BusinessException("取消默认账本设置之前，请先将其他账本设置为默认账本。");
        }
        if(dto.getIsDefault() == BoolEnum.YES){
            update(new LambdaUpdateWrapper<AccountBook>().set(AccountBook::getIsDefault, BoolEnum.NO).eq(AccountBook::getUserId, dto.getUserId()));
        }
        return super.update(dto);
    }
}
