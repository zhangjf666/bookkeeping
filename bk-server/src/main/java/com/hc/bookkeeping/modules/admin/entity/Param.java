package com.hc.bookkeeping.modules.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hc.bookkeeping.common.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 系统参数表
 * </p>
 *
 * @author zjf
 * @since 2020-06-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_param")
@ApiModel(value="Param对象", description="系统参数表")
public class Param extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "参数名")
    private String name;

    @ApiModelProperty(value = "参数值")
    private String value;

    @ApiModelProperty(value = "参数描述")
    private String description;

    @ApiModelProperty(value = "删除标记")
    @TableLogic
    private String delFlag;


}
