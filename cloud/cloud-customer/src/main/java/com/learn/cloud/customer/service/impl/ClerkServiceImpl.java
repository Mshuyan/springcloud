package com.learn.cloud.customer.service.impl;

import com.learn.cloud.customer.service.ClerkService;
import org.springframework.stereotype.Service;

@Service
public class ClerkServiceImpl implements ClerkService {

    @Override
    public void test() {
        System.out.println("测试埋点");
    }
}
