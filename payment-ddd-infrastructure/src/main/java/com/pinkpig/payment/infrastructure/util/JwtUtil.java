package com.pinkpig.payment.infrastructure.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 * 作用：生成令牌、解析令牌
 */
public class JwtUtil {

    // 秘钥 (真实的生产环境应该写在配置文件里，不能硬编码)
    // 只有拥有这个秘钥的人(后端)，才能解密 Token，绝对不能暴露给前端！
    private static final String SECRET_KEY = "pinkpig_secret_key_888";

    // 过期时间 (这里设为 7 天)
    private static final long EXPIRATION_TIME = 7 * 24 * 60 * 60 * 1000L;

    /**
     * 生成 Token
     * @param openId 用户唯一标识
     * @return 加密后的字符串
     */
    public static String createToken(String openId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("openId", openId); // 把关键信息存进去

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis())) // 签发时间
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // 过期时间
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY) // 签名算法
                .compact();
    }

    /**
     * 解析 Token (验证合法性)
     * @param token 加密字符串
     * @return 包含的信息 (Claims)
     */
    public static Map<String, Object> parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从 Token 中获取 OpenID
     */
    public static String getOpenId(String token) {
        Map<String, Object> claims = parseToken(token);
        return (String) claims.get("openId");
    }
}