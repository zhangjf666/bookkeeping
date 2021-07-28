package com.hc.bookkeeping.modules.admin.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/7/1 9:06
 */
@Data
public class LogDto {

    @ApiModelProperty(value = "id")
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
    private String createBy;

    @ApiModelProperty(value = "创建时间", hidden = true)
    private LocalDateTime createTime;
}
