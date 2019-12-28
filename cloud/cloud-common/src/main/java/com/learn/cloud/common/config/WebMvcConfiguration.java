package com.learn.cloud.common.config;

import com.learn.cloud.common.processor.CustomizeArgumentResolverHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * @author shuyan
 */
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {
    /**
     * 增加参数解析器
     * 用于解决不使用注解直接接收某类型参数报没有无参构造方法的问题
     * @param resolvers 已有解析器列表
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new CustomizeArgumentResolverHandler.InstantArgumentResolverHandler());
    }
}
