package com.shopmall.cart.interceptor;

import com.shopmall.auth.entity.UserInfo;
import com.shopmall.auth.utils.JwtUtils;
import com.shopmall.cart.config.JwtProperties;
import com.shopmall.common.utils.CookieUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class UserInterceptor extends HandlerInterceptorAdapter {

    private JwtProperties prop;

    private static final ThreadLocal<UserInfo> tl = new ThreadLocal<>();

    public UserInterceptor(JwtProperties prop) {
        this.prop = prop;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        // 获取cookie
        String token = CookieUtils.getCookieValue(request, prop.getCookieName());
        try {
            // 解析token
            UserInfo user = JwtUtils.getInfoFromToken(token, prop.getPublicKey());
            // 传递user
            tl.set(user);
            // 放行
            return true;
        } catch (Exception e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            log.error("[购物车服务] 解析用户身份失败", e);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 最后用完数据一定要清空
        tl.remove();
    }

    public static UserInfo getUser() {
        return tl.get();
    }
}
