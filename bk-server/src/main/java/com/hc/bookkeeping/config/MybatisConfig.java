package com.hc.bookkeeping.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.OptimisticLockerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.hc.bookkeeping.common.handler.AuditorMetaObjectHandler;
import org.apache.ibatis.session.LocalCacheScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/6/10 8:29
 */
@Configuration
public class MybatisConfig {

    //分页插件
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        PaginationInterceptor page = new PaginationInterceptor();
        page.setDbType(DbType.MYSQL);
        return page;
    }

    @Bean
    public GlobalConfig globalConfiguration(){
        GlobalConfig config = new GlobalConfig();
        GlobalConfig.DbConfig dbConfig = new GlobalConfig.DbConfig();
        dbConfig.setIdType(IdType.AUTO);
        dbConfig.setInsertStrategy(FieldStrategy.NOT_EMPTY);
        dbConfig.setUpdateStrategy(FieldStrategy.NOT_EMPTY);
        dbConfig.setLogicDeleteValue("1");
        dbConfig.setLogicNotDeleteValue("0");
        dbConfig.setLogicDeleteField("del_flag");
        config.setDbConfig(dbConfig);
        return config;
    }

    @Bean
    public MetaObjectHandler metaObjectHandler(){
        return new AuditorMetaObjectHandler();
    }

    /**
     * @Author zjf
     * @Description 乐观锁插件
     * @Date 2020/6/10 10:04
     * @Param []
     * @return com.baomidou.mybatisplus.extension.plugins.OptimisticLockerInterceptor
     **/
    @Bean
    public OptimisticLockerInterceptor optimisticLockerInterceptor() {
        return new OptimisticLockerInterceptor();
    }

    @Bean
    public MybatisConfiguration configuration(){
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setCacheEnabled(false);
        configuration.addInterceptor(paginationInterceptor());
        configuration.addInterceptor(optimisticLockerInterceptor());
        configuration.setCallSettersOnNulls(true);
        configuration.setMapUnderscoreToCamelCase(true); //将查询出的数据库字段变为驼峰命名方式
        configuration.setLocalCacheScope(LocalCacheScope.STATEMENT);
        return configuration;
    }
}
