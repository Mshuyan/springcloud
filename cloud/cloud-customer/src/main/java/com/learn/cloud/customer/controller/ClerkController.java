package com.learn.cloud.customer.controller;

import com.learn.cloud.common.feign.producer.UserFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.Instant;

/**
 * @author shuyan
 */
@Slf4j
@RestController
@RequestMapping("/clerk")
public class ClerkController {
    @Resource
    private UserFeignClient userFeignClient;

    @GetMapping("/customer/name")
    public String getCustomerName(Instant time){
        return userFeignClient.getUserName(time);
    }
}
