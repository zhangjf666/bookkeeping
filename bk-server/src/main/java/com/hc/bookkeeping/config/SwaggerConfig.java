package com.hc.bookkeeping.config;

import com.google.common.base.Predicates;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @Author: zhangjunfeng
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: Created on 2017/11/22 16:11
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket createApi(){
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                //关闭spring的/error接口
                .paths(Predicates.not(PathSelectors.regex("/error.*")))
                .build();
    }

    private ApiInfo apiInfo(){
        return new ApiInfoBuilder()
                .title("assistant api")
                .description("assistant 接口文档")
                .termsOfServiceUrl("")
                .version("1.0")
                .build();
    }
}
