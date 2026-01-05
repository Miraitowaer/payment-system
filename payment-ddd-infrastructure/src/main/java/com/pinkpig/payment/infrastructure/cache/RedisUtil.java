package com.pinkpig.payment.infrastructure.cache;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 写入缓存
     */
    public void set(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    /**
     * 读取缓存
     */
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * ⚡️⚡️ 核心：原子减库存 ⚡️⚡️
     * 相当于 Redis 命令：DECR key
     * @return 减完之后剩余的值
     */
    public Long decrement(String key) {
        return stringRedisTemplate.opsForValue().decrement(key);
    }

    /**
     * 删除缓存
     */
    public void del(String key) {
        stringRedisTemplate.delete(key);
    }

    /**
     * 缓存回滚
     * @param key 商品Key
     * @return
     */
    public Long increment(String key) {
        return stringRedisTemplate.opsForValue().increment(key);
    }

    /**
     * ⚡️ Lua 脚本扣减库存 (原子操作，防超卖)
     * @param key 商品Key
     * @return true=扣减成功; false=库存不足
     */
    public boolean deductStockLua(String key) {
        // 1. 定义 Lua 脚本
        // 逻辑：
        //   a. 先判断 Key 是否存在？不存在直接返回 -1
        //   b. 获取当前库存 stock
        //   c. 如果 stock > 0，执行 DECR 减 1，并返回 1 (成功)
        //   d. 如果 stock <= 0，返回 0 (失败)
        String scriptText =
                "if (redis.call('exists', KEYS[1]) == 0) then return -1 end " +
                        "local stock = tonumber(redis.call('get', KEYS[1])); " +
                        "if (stock > 0) then " +
                        "    redis.call('decr', KEYS[1]); " +
                        "    return 1; " +
                        "else " +
                        "    return 0; " +
                        "end";

        // 2. 构建脚本对象
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(scriptText);
        redisScript.setResultType(Long.class); // 约定脚本返回 Long 类型

        // 3. 执行脚本
        // execute(脚本, Key列表, 参数列表)
        // 这里的 Collections.singletonList(key) 对应脚本里的 KEYS[1]
        Long result = stringRedisTemplate.execute(redisScript, Collections.singletonList(key));

        // 4. 解析结果 (1 表示成功)
        return result != null && result == 1;
    }
}