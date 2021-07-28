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
 * 部门表
 * </p>
 *
 * @author zjf
 * @since 2020-06-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dept")
@ApiModel(value="Dept对象", description="部门表")
public class Dept extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "名称")
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

    @ApiModelProperty(value = "删除标记")
    @TableLogic
    private String delFlag;


}
