package com.hc.bookkeeping.modules.bkeeping.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
public class UserConfigDto implements Serializable {

    @ApiModelProperty(value = "配置id")
    private Long id;

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "配置项目名")
    private String name;

    @ApiModelProperty(value = "配置项的值")
    private String value;

    @ApiModelProperty(value = "描述")
    private String describe;

    @ApiModelProperty(value = "是否启用(0:不启用,1:启用)")
    private BoolEnum enable;

}
