package com.learn.cloud.customer.controller;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

/**
 * @author shuyan
 */
@Slf4j
@RestController
@RequestMapping("/clerk")
public class ClerkController {
    @Resource
    private EurekaClient eurekaClient;
    @Resource
    private RestTemplate restTemplate;

    @GetMapping("/customer/name")
    public String getCustomerName(){
        InstanceInfo instanceInfo = eurekaClient.getNextServerFromEureka("cloud-producer", false);
        String homePageUrl = instanceInfo.getHomePageUrl();
        return restTemplate.getForObject(homePageUrl + "/user/name", String.class);
    }
}
