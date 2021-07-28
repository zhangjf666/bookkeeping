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
 * @Date: 2020/6/30 10:51
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DictDto extends BaseDto {
    @ApiModelProperty(value = "id")
    @NotNull(groups = Update.class, message = "字典id不能为空")
    private Long id;

    @ApiModelProperty(value = "字典名称")
    @NotBlank(groups = Insert.class, message = "字典名称不能为空")
    private String name;

    @ApiModelProperty(value = "字典描述")
    private String description;

    @ApiModelProperty(value = "字典类型")
    private String type;
}
