package com.hc.bookkeeping.modules.bkeeping.service.impl;

import com.hc.bookkeeping.common.base.BaseServiceImpl;
import com.hc.bookkeeping.modules.bkeeping.dto.UserSearchDto;
import com.hc.bookkeeping.modules.bkeeping.entity.UserSearch;
import com.hc.bookkeeping.modules.bkeeping.mapper.UserSearchMapper;
import com.hc.bookkeeping.modules.bkeeping.mapstruct.UserSearchMapstruct;
import com.hc.bookkeeping.modules.bkeeping.service.UserSearchService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户搜索记录表 服务实现类
 * </p>
 *
 * @author zjf
 * @since 2021-08-08
 */
@Service
public class UserSearchServiceImpl extends BaseServiceImpl<UserSearchMapstruct, UserSearchDto,UserSearchMapper, UserSearch> implements UserSearchService {

}
