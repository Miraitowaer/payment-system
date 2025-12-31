package com.pinkpig.payment.domain.auth.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 认证用户实体
 * DDD 视角：这是聚合根，是业务逻辑的载体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthUserEntity {

    /**
     * 对应数据库的主键ID (但在DDD中这只是个属性)
     */
    private Long id;

    /**
     * 系统内部唯一ID (业务主键)
     */
    private String userId;

    /**
     * 微信的OpenId (这是鉴权的核心凭证)
     */
    private String openId;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 注册时间
     */
    private Date createTime;

    // --- 🆚 MVC vs DDD 对比教学 ---
    // MVC模式：这里通常只有get/set，是个“贫血模型”。
    // DDD模式：这里以后会加业务方法。
    // 比如：public boolean isActive() { ... }
    // 或者：public void updateInfo(String nickname, String avatar) { ... }
}