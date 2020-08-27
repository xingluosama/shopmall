package com.shopmall.order.config;

import com.shopmall.auth.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.security.PublicKey;

@Slf4j
@Data
@ConfigurationProperties(prefix = "sm.jwt")
public class JwtProperties {

    private String pubKeyPath;
    private String cookieName;

    private PublicKey publicKey;    // 公钥

    // 对象实例化后，读取公钥和私钥
    @PostConstruct
    public void init() throws Exception {
        try {
            // 读取公钥
            this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        } catch (Exception e) {
            log.error("[订单服务] 初始化公钥失败！", e);
        }
    }
}
