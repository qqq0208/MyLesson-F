package com.mdkj;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@MapperScan("com.mdkj.mapper")
@EnableDiscoveryClient
@SpringBootApplication
public class OrderApp {  
    public static void main(String[] args) {  
        SpringApplication.run(OrderApp.class, args);
    }  
}