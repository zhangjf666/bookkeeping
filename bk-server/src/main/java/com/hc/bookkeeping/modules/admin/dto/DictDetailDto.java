package com.hc.bookkeeping.modules.admin.dto;

import com.hc.bookkeeping.common.base.BaseDto;
import com.hc.bookkeeping.common.support.valid.Insert;
import com.hc.bookkeeping.common.support.valid.Update;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/6/30 10:52
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DictDetailDto extends BaseDto {
    @ApiModelProperty(value = "id")
    @NotNull(groups = Update.class, message = "字典详情id不能为空")
    private Long id;

    @ApiModelProperty(value = "字典id")
    @NotNull(groups = Insert.class, message = "字典id不能为空")
    private Long dictId;

    @ApiModelProperty(value = "数据值")
    @NotBlank(groups = Insert.class, message = "数据值不能为空")
    private String value;

    @ApiModelProperty(value = "数据名称")
    @NotBlank(groups = Insert.class, message = "数据名称不能为空")
    private String name;

    @ApiModelProperty(value = "排序")
    private Integer sort;

    @ApiModelProperty(value = "上级id")
    private Long pid;
}
