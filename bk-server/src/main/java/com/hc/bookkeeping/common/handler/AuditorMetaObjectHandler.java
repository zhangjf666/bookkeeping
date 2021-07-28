package com.hc.bookkeeping.common.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.hc.bookkeeping.common.utils.SpringSecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/6/4 13:56
 */
@Slf4j
public class AuditorMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("start auditor insert fill");
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "createBy", String.class, getCurrentUsername());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateBy", String.class, getCurrentUsername());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("start auditor update fill");
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updateBy", String.class, getCurrentUsername());
    }

    private String getCurrentUsername(){
        try {
            return SpringSecurityUtil.getCurrentUsername();
        } catch (Exception e) {
            log.debug("无法获取用户名,原因:{},将使用system系统用户名",e.getMessage());
            return "system";
        }
    }
}
