package com.hc.bookkeeping.modules.admin.mapstruct;

import com.hc.bookkeeping.common.base.BaseMapstruct;
import com.hc.bookkeeping.common.support.mapstruct.MapStructTransform;
import com.hc.bookkeeping.modules.admin.dto.UserDto;
import com.hc.bookkeeping.modules.admin.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/6/9 10:08
 */
@Mapper(componentModel = "spring",uses = {RoleMapstruct.class, DeptMapstruct.class, PositionMapstruct.class, MapStructTransform.class},unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapstruct extends BaseMapstruct<UserDto, User> {
}
