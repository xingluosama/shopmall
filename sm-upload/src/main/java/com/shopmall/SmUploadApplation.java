package com.shopmall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class SmUploadApplation {
    public static void main(String[] args) {
        SpringApplication.run(SmUploadApplation.class);
    }
}
