package com.pinkpig.payment.infrastructure.config;

import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 微信配置类
 * 作用：将application.yml里的配置读取出来，并构建 WxMpService 实例放到 Spring 容器中
 */
@Configuration
public class WeChatConfig {

    @Value("${wx.config.app-id}")
    private String appId;

    @Value("${wx.config.app-secret}")
    private String appSecret;

    @Value("${wx.config.token}")
    private String token;

    @Bean
    public WxMpService wxMpService() {
        // 1. 配置存储 (用来存 appId, secret, token)
        WxMpDefaultConfigImpl configStorage = new WxMpDefaultConfigImpl();
        configStorage.setAppId(appId);
        configStorage.setSecret(appSecret);
        configStorage.setToken(token);

        // 2. 创建 Service 实例
        WxMpService wxMpService = new WxMpServiceImpl();
        wxMpService.setWxMpConfigStorage(configStorage);

        // 3. 返回实例，Spring 会把它接管，之后我们在 Infra 层就可以 @Resource 注入它了
        return wxMpService;
    }
}