package com.hc.bookkeeping.modules.bkeeping.dto;

import com.hc.bookkeeping.common.annotation.Query;
import com.hc.bookkeeping.common.annotation.Sort;
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
public class ClassifyQueryDto implements Serializable {

    @ApiModelProperty(value = "id")
    @Query
    private Long id;

    @ApiModelProperty(value = "名称")
    @Query(match = Query.Matching.INNER_LIKE)
    private String name;

    @ApiModelProperty(value = "父类id")
    @Query
    private Long pid;

    @ApiModelProperty(value = "所属用户id")
    @Query(column = "user_id")
    private Long userId;

    @ApiModelProperty(value = "排序")
    @Sort
    private Integer sort;

    @ApiModelProperty(value = "类型(0:支出,1:收入)")
    @Query
    private BillType type;

    @ApiModelProperty(value = "是否启用(0:不启用,1:启用)")
    @Query
    private BoolEnum enable;
}
