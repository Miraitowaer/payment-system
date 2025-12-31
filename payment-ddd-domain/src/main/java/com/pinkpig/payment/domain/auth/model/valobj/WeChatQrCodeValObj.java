package com.pinkpig.payment.domain.auth.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 微信二维码值对象
 * DDD 视角：它不需要唯一ID，属性变了就是另一个对象了
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeChatQrCodeValObj {

    /**
     * 获取二维码的凭证
     */
    private String ticket;

    /**
     * 二维码解析后的地址 (前端转成二维码图片用)
     */
    private String codeUrl;

    /**
     * 过期时间 (秒)
     */
    private Integer expireSeconds;
}