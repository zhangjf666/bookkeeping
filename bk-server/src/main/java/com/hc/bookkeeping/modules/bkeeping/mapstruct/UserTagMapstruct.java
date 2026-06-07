package com.hc.bookkeeping.modules.bkeeping.mapstruct;

import com.hc.bookkeeping.common.base.BaseMapstruct;
import com.hc.bookkeeping.modules.bkeeping.dto.UserRemarkDto;
import com.hc.bookkeeping.modules.bkeeping.dto.UserTagDto;
import com.hc.bookkeeping.modules.bkeeping.entity.UserRemark;
import com.hc.bookkeeping.modules.bkeeping.entity.UserTag;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserTagMapstruct extends BaseMapstruct<UserTagDto, UserTag> {
}
