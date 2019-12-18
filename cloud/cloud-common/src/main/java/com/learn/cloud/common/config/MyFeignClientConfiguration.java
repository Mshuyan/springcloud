package com.learn.cloud.common.config;

import feign.Contract;
import org.springframework.context.annotation.Bean;

/**
 * @author will
 */
public class MyFeignClientConfiguration {

    @Bean
    public Contract contract(){
        return new Contract.Default();
    }
}
