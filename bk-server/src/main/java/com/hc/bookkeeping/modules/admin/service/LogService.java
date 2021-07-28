package com.hc.bookkeeping.modules.admin.service;

import com.hc.bookkeeping.common.base.BaseService;
import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.modules.admin.dto.LogDto;
import com.hc.bookkeeping.modules.admin.dto.LogQueryDto;
import com.hc.bookkeeping.modules.admin.entity.Log;
import org.springframework.scheduling.annotation.Async;

/**
 * <p>
 * 日志表 服务类
 * </p>
 *
 * @author zjf
 * @since 2020-06-11
 */
public interface LogService extends BaseService<LogDto, Log> {

    /**
     * 分页查询
     * @param queryDto 查询条件
     * @param page 分页
     * @return
     */
    Page queryPage(LogQueryDto queryDto, Page page);

    /**
     * 异步保存日志
     * @param log 日志实体
     */
    @Async
    void asyncSave(Log log);
}
