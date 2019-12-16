package com.learn.cloud.common.feign.producer.impl;

import com.learn.cloud.common.feign.producer.UserFeignClient;

import java.time.Instant;

/**
 * @author will
 */
public class UserFeignClientImpl implements UserFeignClient {
    @Override
    public String getUserName(Instant time) {
        return "请求超时";
    }
}
