package com.pinkpig.payment.infrastructure.adapter.repository;

import com.pinkpig.payment.domain.auth.repository.ILoginRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Repository // 标记这是仓储层的实现
public class LoginRepositoryImpl implements ILoginRepository {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final String REDIS_PREFIX = "login:ticket:";

    @Override
    public void saveLoginState(String ticket, String openId) {
        // Key: login:ticket:gQZN...
        // Value: o8A9u...
        // 过期时间：10分钟 (和二维码有效期一致)
        stringRedisTemplate.opsForValue().set(
                REDIS_PREFIX + ticket,
                openId,
                600,
                TimeUnit.SECONDS
        );
    }

    @Override
    public String checkLoginState(String ticket) {
        return stringRedisTemplate.opsForValue().get(REDIS_PREFIX + ticket);
    }
}