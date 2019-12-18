package com.learn.cloud.common.feign.producer;

import com.learn.cloud.common.constants.FeignClientNameConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;

/**
 * @author shuyan
 */
@FeignClient(value = FeignClientNameConstants.CLOUD_PRODUCER,path = "/user")
public interface UserFeignClient {
    /**
     * 获取用户名
     * @param time 时间
     * @return 用户名
     */
    @GetMapping("/name")
    String getUserName(@RequestParam("time") Instant time);
}
