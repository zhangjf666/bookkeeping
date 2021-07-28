package com.hc.bookkeeping.modules.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 日志表
 * </p>
 *
 * @author zjf
 * @since 2020-06-11
 */
@Data
@TableName("sys_log")
@ApiModel(value="Log对象", description="日志表")
public class Log implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "日志类型")
    private String type;

    @ApiModelProperty(value = "日志标题")
    private String title;

    @ApiModelProperty(value = "操作IP地址")
    private String ip;

    @ApiModelProperty(value = "浏览器")
    private String browser;

    @ApiModelProperty(value = "用户代理")
    private String userAgent;

    @ApiModelProperty(value = "请求URI")
    private String requestUri;

    @ApiModelProperty(value = "操作方式")
    private String method;

    @ApiModelProperty(value = "操作提交的数据")
    private String params;

    @ApiModelProperty(value = "请求时间")
    private LocalDateTime requestTime;

    @ApiModelProperty(value = "执行时长(ms)")
    private Long executeTime;

    @ApiModelProperty(value = "返回内容")
    private String response;

    @ApiModelProperty(value = "异常信息")
    private String exception;

    @ApiModelProperty(value = "日志等级")
    private String level;

    @ApiModelProperty(value = "创建人", hidden = true)
    @TableField(value = "create_by",fill = FieldFill.INSERT)
    private String createBy;

    @ApiModelProperty(value = "创建时间", hidden = true)
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
