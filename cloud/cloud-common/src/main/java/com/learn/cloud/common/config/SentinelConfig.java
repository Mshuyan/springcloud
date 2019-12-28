package com.learn.cloud.common.config;

import com.alibaba.cloud.sentinel.annotation.SentinelRestTemplate;
import com.learn.cloud.common.processor.BlockExceptionProcessor;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class SentinelConfig {

    @Bean
    @LoadBalanced
    @SentinelRestTemplate(blockHandler = "handleException", blockHandlerClass = BlockExceptionProcessor.class)
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
