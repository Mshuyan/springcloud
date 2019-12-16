package com.learn.cloud.producer.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/**
 * @author shuyan
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @GetMapping("/name")
    public String getUserName(Instant time){
        return time.toString();
    }
}
