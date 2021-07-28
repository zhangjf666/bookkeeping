package com.hc.bookkeeping.modules.bkeeping.service;

import com.hc.bookkeeping.common.base.BaseService;
import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.modules.admin.dto.DictDetailQueryDto;
import com.hc.bookkeeping.modules.bkeeping.dto.AccountBookDto;
import com.hc.bookkeeping.modules.bkeeping.dto.AccountBookQueryDto;
import com.hc.bookkeeping.modules.bkeeping.entity.AccountBook;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Set;

/**
 * <p>
 * 账本 服务类
 * </p>
 *
 * @author zjf
 * @since 2021-07-27
 */
public interface AccountBookService extends BaseService<AccountBookDto, AccountBook> {

    /**
     * 分页查询
     * @param queryDto 查询条件
     * @param page 分页
     * @return
     */
    Page queryPage(AccountBookQueryDto queryDto, Page page);
}
