package com.pinkpig.payment.domain.auth.service;

import com.pinkpig.payment.domain.auth.model.valobj.WeChatCallbackVO;
import com.pinkpig.payment.domain.auth.model.valobj.WeChatQrCodeValObj;

/**
 * 微信服务接口 (防腐层/适配器接口)
 * 作用：Domain层不需要知道Http怎么发，只需要结果
 */
public interface IWeChatService {

    /**
     * 获取微信登录二维码
     */
    WeChatQrCodeValObj getQrCodeTicket();

    /**
     * 校验回调信息，返回 OpenId
     */
    WeChatCallbackVO checkLogin(String callbackXml);

    /**
     * 根据Token获取用户信息 (后续回调时用到)
     */
    // AuthUserEntity getUserInfo(String accessToken, String openId); 暂时先不写，一步步来
}