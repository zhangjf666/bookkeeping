package com.hc.bookkeeping.modules.bkeeping.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户常用备注表
 * </p>
 *
 * @author zjf
 * @since 2023-06-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="UserRemark对象", description="用户常用备注表")
public class UserRemarkDto implements Serializable {

    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "备注内容")
    private String remark;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "所属分类id")
    private Long classifyId;
}
