package com.shopmall.auth.config;

import com.shopmall.auth.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

@Slf4j
@Data
@ConfigurationProperties(prefix = "sm.jwt")
public class JwtProperties {

    private String secret;
    private String pubKeyPath;
    private String priKeyPath;
    private int expire;
    private String cookieName;
    private int cookieMaxAge;

    private PublicKey publicKey;    // 公钥
    private PrivateKey privateKey;  // 私钥

    // 对象实例化后，读取公钥和私钥
    @PostConstruct
    public void init() throws Exception {

        try {
            // 公钥私钥如果不存在，先生成
            File pubPath = new File(pubKeyPath);
            File priPath = new File(priKeyPath);
            if (!pubPath.exists() || !priPath.exists()) {
                // 生成公钥和私钥
                RsaUtils.generateKey(pubKeyPath, priKeyPath, secret);
            }

            // 读取公钥和私钥
            this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
            this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
        } catch (Exception e) {
            log.error("[授权中心] 初始化公钥和私钥失败！", e);
            throw new RuntimeException();
        }
    }
}
