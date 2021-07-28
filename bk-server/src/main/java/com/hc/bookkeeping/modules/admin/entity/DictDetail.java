package com.hc.bookkeeping.modules.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hc.bookkeeping.common.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 字典明细表
 * </p>
 *
 * @author zjf
 * @since 2020-06-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_detail")
@ApiModel(value="DictDetail对象", description="字典明细表")
public class DictDetail extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "字典id")
    private Long dictId;

    @ApiModelProperty(value = "数据值")
    private String value;

    @ApiModelProperty(value = "数据名称")
    private String name;

    @ApiModelProperty(value = "排序")
    private Integer sort;

    @ApiModelProperty(value = "上级id")
    private Long pid;


}
