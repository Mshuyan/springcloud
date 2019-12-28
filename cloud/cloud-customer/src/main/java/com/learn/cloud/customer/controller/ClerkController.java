package com.learn.cloud.customer.controller;

import com.learn.cloud.common.entity.DeptEntity;
import com.learn.cloud.common.feign.producer.UserFeignClient;
import com.learn.cloud.customer.service.ClerkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.Instant;

/**
 * @author shuyan
 */
@Slf4j
@RefreshScope
@RestController
@RequestMapping("/clerk")
public class ClerkController {
    @Resource
    private UserFeignClient userFeignClient;
    @Value("${customer.test}")
    private String test;
    @Resource
    private ClerkService clerkServiceImpl;

    @GetMapping("/customer/name")
    public String getCustomerName(@RequestParam Instant time){
        System.out.println(test);
        clerkServiceImpl.test();
        return userFeignClient.getUserName(time);
    }

    @GetMapping("/test1")
    public String test1(DeptEntity entity){
        return entity.getTime().toString();
    }

    @PostMapping("/test1")
    public DeptEntity test2(@RequestBody DeptEntity entity){
        return entity;
    }

    @GetMapping("/test2")
    public Instant test3(Instant time){
        return time;
    }
}
