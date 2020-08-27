package com.shopmall.auth.web;

import com.shopmall.auth.config.JwtProperties;
import com.shopmall.auth.entity.UserInfo;
import com.shopmall.auth.service.AuthService;
import com.shopmall.auth.utils.JwtUtils;
import com.shopmall.common.enums.ExceptionEnum;
import com.shopmall.common.exception.SmException;
import com.shopmall.common.utils.CookieUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtProperties prop;

    /**
     * 登录授权
     * @param username
     * @param password
     * @return
     */
    @PostMapping("login")
    public ResponseEntity<Void> login(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpServletRequest request,
            HttpServletResponse response) {
        // 登录
        String token = authService.login(username, password);
        // 写入cookie
        CookieUtils.setCookie(request, response, prop.getCookieName(),
                token, prop.getCookieMaxAge(), true, true);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("verify")
    public ResponseEntity<UserInfo> verify(
            @CookieValue("SM_TOKEN") String token,
            HttpServletRequest request,
            HttpServletResponse response) {
        try {
            // 解析token
            UserInfo info = JwtUtils.getInfoFromToken(token, prop.getPublicKey());

            // 刷新token，重新生成token
            String newToken = JwtUtils.generateToken(info, prop.getPrivateKey(), prop.getExpire());

            // 写入cookie中
            CookieUtils.setCookie(request, response, prop.getCookieName(),
                    newToken, prop.getCookieMaxAge(), true, true);

            // 已登录，返回用户信息
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            // token已过期，或者token被篡改
            throw new SmException(ExceptionEnum.UNAUTHORIZED);
        }
    }
}
