package com.pinkpig.payment.trigger.http.interceptor;

import com.pinkpig.payment.infrastructure.util.JwtUtil;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 鉴权拦截器
 * 作用：拦截所有请求，检查 Header 里有没有 Token
 */
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 1. 获取 Token (约定放在 Header 的 "Authorization" 字段里)
        String token = request.getHeader("Authorization");

        // 2. 如果没传 Token -> 401
        if (token == null || token.isEmpty()) {
            response.setStatus(401);
            response.getWriter().write("Token is missing");
            return false; // 拦截，不让进
        }

        // 3. 校验 Token -> 如果非法或过期 -> 401
        try {
            String openId = JwtUtil.getOpenId(token);

            // 【关键】把解析出来的 openId 存入 Request 作用域
            // 这样 Controller 里就能直接拿出当前是谁在操作，不用再解析一遍
            request.setAttribute("openId", openId);

            return true; // 放行
        } catch (Exception e) {
            response.setStatus(401);
            response.getWriter().write("Invalid Token");
            return false;
        }
    }
}