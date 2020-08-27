package com.shopmall.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

@EnableZuulProxy
@SpringCloudApplication
public class SmGateway {
    public static void main(String[] args) {
        SpringApplication.run(SmGateway.class);
    }
}
