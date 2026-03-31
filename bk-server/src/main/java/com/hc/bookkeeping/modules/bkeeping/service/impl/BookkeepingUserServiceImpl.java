package com.hc.bookkeeping.modules.bkeeping.service.impl;

import com.hc.bookkeeping.common.base.BaseServiceImpl;
import com.hc.bookkeeping.modules.bkeeping.dto.BookkeepingUserDto;
import com.hc.bookkeeping.modules.bkeeping.dto.UserSearchDto;
import com.hc.bookkeeping.modules.bkeeping.entity.BookkeepingUser;
import com.hc.bookkeeping.modules.bkeeping.entity.UserSearch;
import com.hc.bookkeeping.modules.bkeeping.mapper.BookkeepingUserMapper;
import com.hc.bookkeeping.modules.bkeeping.mapper.UserSearchMapper;
import com.hc.bookkeeping.modules.bkeeping.mapstruct.BookkeepingUserMapstruct;
import com.hc.bookkeeping.modules.bkeeping.mapstruct.UserSearchMapstruct;
import com.hc.bookkeeping.modules.bkeeping.service.BookkeepingUserService;
import com.hc.bookkeeping.modules.bkeeping.service.UserSearchService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 记账用户表 服务实现类
 * </p>
 *
 * @author zjf
 * @since 2021-08-08
 */
@Service
public class BookkeepingUserServiceImpl extends BaseServiceImpl<BookkeepingUserMapstruct, BookkeepingUserDto, BookkeepingUserMapper, BookkeepingUser> implements BookkeepingUserService {

}
