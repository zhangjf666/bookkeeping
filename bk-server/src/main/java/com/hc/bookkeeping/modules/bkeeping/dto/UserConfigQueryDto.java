package com.hc.bookkeeping.modules.bkeeping.dto;

import com.hc.bookkeeping.common.annotation.Query;
import com.hc.bookkeeping.common.model.BoolEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <p>
 * 用户配置表
 * </p>
 *
 * @author zjf
 * @since 2021-07-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="UserConfig对象", description="用户配置表")
public class UserConfigQueryDto implements Serializable {

    @ApiModelProperty(value = "配置id")
    @Query
    private Long id;

    @ApiModelProperty(value = "用户id")
    @Query(column = "user_id")
    private Long userId;

    @ApiModelProperty(value = "配置项目名")
    @Query
    private String name;

    @ApiModelProperty(value = "描述")
    @Query(match = Query.Matching.INNER_LIKE)
    private String description;

    @ApiModelProperty(value = "是否启用(0:不启用,1:启用)")
    @Query
    private BoolEnum enable;

}
