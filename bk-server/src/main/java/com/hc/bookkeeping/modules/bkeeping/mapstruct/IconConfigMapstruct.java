package com.hc.bookkeeping.modules.bkeeping.mapstruct;

import com.hc.bookkeeping.common.base.BaseMapstruct;
import com.hc.bookkeeping.modules.bkeeping.dto.IconConfigDto;
import com.hc.bookkeeping.modules.bkeeping.entity.IconConfig;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IconConfigMapstruct extends BaseMapstruct<IconConfigDto, IconConfig> {
}
