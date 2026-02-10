package com.hc.bookkeeping.modules.bkeeping.mapstruct;

import com.hc.bookkeeping.common.base.BaseMapstruct;
import com.hc.bookkeeping.modules.bkeeping.dto.UserRemarkDto;
import com.hc.bookkeeping.modules.bkeeping.entity.UserRemark;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserRemarkMapstruct extends BaseMapstruct<UserRemarkDto, UserRemark> {
}
