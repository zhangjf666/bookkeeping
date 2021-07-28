package com.hc.bookkeeping.modules.admin.mapstruct;

import com.hc.bookkeeping.common.base.BaseMapstruct;
import com.hc.bookkeeping.modules.admin.dto.RoleDto;
import com.hc.bookkeeping.modules.admin.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/6/9 10:09
 */
@Mapper(componentModel = "spring", uses = {MenuMapstruct.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleMapstruct extends BaseMapstruct<RoleDto, Role> {
}
