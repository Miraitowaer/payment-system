package com.pinkpig.payment.domain.auth.repository;

import com.pinkpig.payment.domain.auth.model.entity.AuthUserEntity;

/**
 * 认证仓储接口
 * 作用：把对数据库的操作抽象出来
 */
public interface IAuthRepository {

    /**
     * 根据OpenId查询用户信息
     */
    AuthUserEntity queryUserByOpenId(String openId);

    /**
     * 保存/注册新用户
     */
    void saveUser(AuthUserEntity authUserEntity);
}