package com.hc.bookkeeping.modules.bkeeping.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hc.bookkeeping.common.model.BoolEnum;
import com.hc.bookkeeping.modules.bkeeping.model.BillType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <p>
 * 分类表
 * </p>
 *
 * @author zjf
 * @since 2021-07-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="Classify对象", description="分类表")
public class ClassifyDto implements Serializable {

    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "父类id")
    private Long pid;

    @ApiModelProperty(value = "所属用户id")
    private Long userId;

    @ApiModelProperty(value = "图标名称")
    private String image;

    @ApiModelProperty(value = "排序")
    private Integer sort;

    @ApiModelProperty(value = "类型(0:支出,1:收入)")
    private BillType type;

    @ApiModelProperty(value = "是否启用(0:不启用,1:启用)")
    private BoolEnum enable;
}
