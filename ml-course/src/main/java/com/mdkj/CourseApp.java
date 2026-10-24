package com.mdkj;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author 油桃老师
 **/

@MapperScan(basePackages = "com.mdkj.mapper")
@EnableDiscoveryClient
@SpringBootApplication
public class CourseApp {
    public static void main(String[] args) {
        SpringApplication.run(CourseApp.class,args);
    }
}
