package com.pinkpig.payment.domain.auth.repository;

public interface ILoginRepository {

    /**
     * 保存登录凭证状态
     * @param ticket  二维码的票据 (Key)
     * @param openId  用户的OpenID (Value)
     */
    void saveLoginState(String ticket, String openId);

    /**
     * 查询登录凭证状态
     * @param ticket 二维码的票据
     * @return openId (如果没扫码，返回 null)
     */
    String checkLoginState(String ticket);
}