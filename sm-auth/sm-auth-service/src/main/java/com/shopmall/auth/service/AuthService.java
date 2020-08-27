package com.shopmall.auth.service;

import com.shopmall.auth.client.UserClient;
import com.shopmall.auth.config.JwtProperties;
import com.shopmall.auth.entity.UserInfo;
import com.shopmall.auth.utils.JwtUtils;
import com.shopmall.common.enums.ExceptionEnum;
import com.shopmall.common.exception.SmException;
import com.shopmall.user.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@EnableConfigurationProperties(JwtProperties.class)
public class AuthService {

    @Autowired
    private UserClient userClient;

    @Autowired
    private JwtProperties prop;

    public String login(String username, String password) {

        try {
            // 校验用户名和密码
            User user = userClient.queryUserByUsernameAndPassword(username, password);
            // 判断
            if (user == null) {
                throw new SmException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
            }
            // 生成token
            String token = JwtUtils.generateToken(new UserInfo(user.getId(), username), prop.getPrivateKey(), prop.getExpire());

            return token;
        } catch (Exception e) {
            log.error("[授权中心] 用户名或密码有误，用户名称{}", username, e);
            throw new SmException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }

    }
}
