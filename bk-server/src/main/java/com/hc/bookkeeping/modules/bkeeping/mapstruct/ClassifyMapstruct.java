package com.hc.bookkeeping.modules.bkeeping.mapstruct;

import com.hc.bookkeeping.common.base.BaseMapstruct;
import com.hc.bookkeeping.modules.bkeeping.dto.ClassifyDto;
import com.hc.bookkeeping.modules.bkeeping.entity.Classify;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClassifyMapstruct extends BaseMapstruct<ClassifyDto, Classify> {
}
