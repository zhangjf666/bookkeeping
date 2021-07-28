package com.hc.bookkeeping.modules.admin.dto;

import com.hc.bookkeeping.common.annotation.Query;
import com.hc.bookkeeping.common.annotation.Sort;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/6/30 10:57
 */
@Data
public class LogQueryDto {
    @ApiModelProperty(value = "日志类型")
    @Query
    private String type;

    @ApiModelProperty(value = "操作IP地址")
    @Query(match = Query.Matching.INNER_LIKE)
    private String ip;

    @ApiModelProperty(value = "日志等级")
    @Query
    private String level;

    @ApiModelProperty(value = "创建人", hidden = true)
    @Query(column = "create_by")
    private String createBy;

    @ApiModelProperty(value = "创建时间", hidden = true)
    @Query(column = "create_time", match = Query.Matching.BETWEEN)
    @Sort(column = "create_time", sort = Sort.SortType.DESC)
    private List<LocalDateTime> createTime;
}

