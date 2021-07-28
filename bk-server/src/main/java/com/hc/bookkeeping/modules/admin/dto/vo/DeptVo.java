package com.hc.bookkeeping.modules.admin.dto.vo;

import com.hc.bookkeeping.common.base.SimpleLazyTree;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/8/4 14:22
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DeptVo extends SimpleLazyTree<DeptVo, Long> {

    @ApiModelProperty(value = "名称")
    private String name;

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

    public void setSort(Integer sort) {
        this.sort = sort;
        this.weight = sort;
    }

    private String createBy;

    private LocalDateTime createTime;

    private String updateBy;

    private LocalDateTime updateTime;
}