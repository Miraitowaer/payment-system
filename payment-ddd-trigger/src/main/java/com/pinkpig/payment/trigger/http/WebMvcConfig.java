package com.pinkpig.payment.trigger.http;

import com.pinkpig.payment.trigger.http.interceptor.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor())
                .addPathPatterns("/api/**") // 拦截所有 api 接口
                .excludePathPatterns(
                        "/api/v1/login/weixin_code",     // 放行：获取二维码
                        "/api/v1/login/weixin_callback", // 放行：微信回调
                        "/api/v1/login/check_login"      // 放行：轮询查询
                );
    }
}