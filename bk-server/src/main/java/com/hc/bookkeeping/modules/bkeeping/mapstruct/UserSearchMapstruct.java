package com.hc.bookkeeping.modules.bkeeping.mapstruct;

import com.hc.bookkeeping.common.base.BaseMapstruct;
import com.hc.bookkeeping.modules.bkeeping.dto.UserSearchDto;
import com.hc.bookkeeping.modules.bkeeping.entity.UserSearch;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserSearchMapstruct extends BaseMapstruct<UserSearchDto, UserSearch> {
}
