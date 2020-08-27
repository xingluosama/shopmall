package com.shopmall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.shopmall.user.mapper")
public class SmUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmUserApplication.class, args);
    }
}
