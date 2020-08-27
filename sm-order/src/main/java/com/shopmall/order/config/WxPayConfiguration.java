package com.shopmall.order.config;

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConstants;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WxPayConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "sm.pay")
    public PayConfig payConfig() {
        return new PayConfig();
    }

    @Bean
    public WXPay wxPay(PayConfig payConfig) {
        return new WXPay(payConfig, WXPayConstants.SignType.HMACSHA256);
    }
}
