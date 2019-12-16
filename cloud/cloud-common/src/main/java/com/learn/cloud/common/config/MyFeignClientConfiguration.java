package com.learn.cloud.common.config;

import feign.Retryer;
import org.springframework.context.annotation.Bean;

/**
 * @author will
 */
public class MyFeignClientConfiguration {

    @Bean
    public Retryer retryer(){
        return new Retryer.Default();
    }
}
