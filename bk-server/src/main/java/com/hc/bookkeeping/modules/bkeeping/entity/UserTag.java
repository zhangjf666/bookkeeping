package com.hc.bookkeeping.modules.bkeeping.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 用户标签表
 * </p>
 *
 * @author zjf
 * @since 2026-02-11
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("user_tag")
@ApiModel(value="UserTag对象", description="用户标签表")
public class UserTag implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "标签代码")
    private Long code;

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "标签名称")
    private String name;

    @ApiModelProperty(value = "标签颜色")
    private String color;

    @ApiModelProperty(value = "是否置顶(0:不置顶,1:置顶)")
    private String pinned;

    @ApiModelProperty(value = "创建时间")
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    @TableField(value = "update_time",fill = FieldFill.INSERT_UPDATE)
    @Version
    private LocalDateTime updateTime;

}
