package com.hc.bookkeeping.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @Author: zhangjunfeng
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: Created on 2017/12/29 9:59
 */
@Slf4j
@Component
public class SpringContextUtil implements ApplicationContextAware, DisposableBean {

    private static ApplicationContext applicationContext = null;
    
    public static ApplicationContext getApplicationContext(){
        assertContext();
        return applicationContext;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(String beanName){
        assertContext();
        return (T)applicationContext.getBean(beanName);
    }

    public static <T> T getBean(Class<T> classType){
        assertContext();
        return applicationContext.getBean(classType);
    }

    public static void release(){
        log.info("释放ApplicationContext.");
        SpringContextUtil.applicationContext = null;
    }

    @Override
    public void destroy() throws Exception {
        SpringContextUtil.release();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        log.info("注入ApplicationContext.");
        SpringContextUtil.applicationContext = applicationContext;
    }

    /**
     * 检查ApplicationContext不为空.
     */
    private static void assertContext() {
        Validate.validState(applicationContext != null, "ApplicaitonContext 属性未注入!");
    }
}
