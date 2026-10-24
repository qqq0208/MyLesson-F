package com.mdkj;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author 油桃老师
 **/
@EnableDiscoveryClient
@SpringBootApplication
@MapperScan(basePackages = "com.mdkj.mapper")
public class UserApp {
    public static void main(String[] args) {
        SpringApplication.run(UserApp.class,args);
    }
}
