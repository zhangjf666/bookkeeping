package com.hc.bookkeeping.modules.admin.dto;

import com.hc.bookkeeping.common.base.BaseDto;
import com.hc.bookkeeping.common.support.valid.Insert;
import com.hc.bookkeeping.common.support.valid.Update;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/6/8 16:14
 */
@Data
public class DeptDto extends BaseDto {

    @ApiModelProperty(value = "id")
    @NotNull(groups = Update.class, message = "部门id不能为空")
    private Long id;

    @ApiModelProperty(value = "名称")
    @NotBlank(groups = Insert.class, message = "部门名称不能为空")
    private String name;

    @ApiModelProperty(value = "上级部门id")
    private Long pid;

    @ApiModelProperty(value = "排序")
    private Integer sort;

    @ApiModelProperty(value = "部门类型")
    private String type;

    @ApiModelProperty(value = "联系地址")
    private String address;

    @ApiModelProperty(value = "邮政编码")
    private String zipCode;

    @ApiModelProperty(value = "负责人")
    private String master;

    @ApiModelProperty(value = "电话")
    private String phone;

    @ApiModelProperty(value = "传真")
    private String fax;

    @ApiModelProperty(value = "邮箱")
    private String email;

    @ApiModelProperty(value = "状态(1:启用 0:禁用)")
    private String enabled;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeptDto deptDto = (DeptDto) o;
        return Objects.equals(id, deptDto.id) &&
                Objects.equals(name, deptDto.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
