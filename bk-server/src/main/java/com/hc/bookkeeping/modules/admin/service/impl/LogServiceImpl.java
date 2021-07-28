package com.hc.bookkeeping.modules.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hc.bookkeeping.common.base.BaseServiceImpl;
import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.common.utils.QueryUtil;
import com.hc.bookkeeping.modules.admin.dto.LogDto;
import com.hc.bookkeeping.modules.admin.dto.LogQueryDto;
import com.hc.bookkeeping.modules.admin.entity.Log;
import com.hc.bookkeeping.modules.admin.mapper.LogMapper;
import com.hc.bookkeeping.modules.admin.mapstruct.LogMapStruct;
import com.hc.bookkeeping.modules.admin.service.LogService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 日志表 服务实现类
 * </p>
 *
 * @author zjf
 * @since 2020-06-11
 */
@Service
public class LogServiceImpl extends BaseServiceImpl<LogMapStruct, LogDto, LogMapper, Log> implements LogService {

    @Override
    public Page queryPage(LogQueryDto queryDto, Page page) {
        QueryWrapper<Log> wrapper = (QueryWrapper<Log>) QueryUtil.<LogQueryDto,Log>bulid(queryDto);
        //返回跟异常字段内容太大,列表查询时排除
        wrapper.select("id","type","title","ip","browser","user_agent","request_uri","request_time","execute_time",
                "level","create_by","create_time");
        Page rpage = Page.fromMybatisPlusPage(baseMapper.selectPage(page.toMybatisPlusPage(), wrapper));
        rpage.setRecord(baseMapstruct.toDto(rpage.getRecord()));
        return rpage;
    }

    @Override
    public void asyncSave(Log log) {
        baseMapper.insert(log);
    }
}
