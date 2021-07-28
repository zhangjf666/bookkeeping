package com.hc.bookkeeping.modules.admin.mapstruct;

import com.hc.bookkeeping.common.base.BaseMapstruct;
import com.hc.bookkeeping.modules.admin.dto.DictDto;
import com.hc.bookkeeping.modules.admin.entity.Dict;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/6/30 10:53
 */
@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DictMapstruct extends BaseMapstruct<DictDto, Dict> {
}
