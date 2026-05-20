package com.liang.bbs.rest.config.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    /**
     * 文档首页概述
     *
     * @return
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("社区论坛（开源版/个人版） Restful API 文档")
                        .version(ApiVersionConstant.V_LATEST)
                        .description("社区论坛，是社区系列元老级系统，也是梦开始的地方！")
                        .contact(new Contact().name("社区")
                                .url("nansheng")
                                .email("924818949@qq.com")));
    }

//    @Bean
//    public GroupedOpenApi publicApi() {
//        return GroupedOpenApi.builder()
//                .group("default")
//                .pathsToMatch("/api/**")
//                .build();
//    }


}
