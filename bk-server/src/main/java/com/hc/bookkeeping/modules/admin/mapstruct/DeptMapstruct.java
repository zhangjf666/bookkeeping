package com.hc.bookkeeping.modules.admin.mapstruct;

import com.hc.bookkeeping.common.base.BaseMapstruct;
import com.hc.bookkeeping.modules.admin.dto.DeptDto;
import com.hc.bookkeeping.modules.admin.entity.Dept;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/6/9 10:10
 */
@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DeptMapstruct extends BaseMapstruct<DeptDto, Dept> {
}
