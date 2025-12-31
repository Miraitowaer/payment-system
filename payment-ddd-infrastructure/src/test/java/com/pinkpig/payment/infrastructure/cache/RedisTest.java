package com.pinkpig.payment.infrastructure.cache;

import com.pinkpig.payment.infrastructure.TestApplication;
import com.pinkpig.payment.infrastructure.cache.RedisUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest(classes = TestApplication.class)
public class RedisTest {

    @Resource
    private RedisUtil redisUtil;

    @Test
    public void testStockDeduct() {
        // 1. 初始化库存：假设商品 1001 有 10 个库存
        // 在 Redis 里 Key 命名规范通常是：业务:ID
        String stockKey = "goods:stock:1001";
        redisUtil.set(stockKey, "10");

        System.out.println("🔥 初始化库存: " + redisUtil.get(stockKey));

        // 2. 模拟用户购买，扣减 1 个
        Long leftStock = redisUtil.decrement(stockKey);
        System.out.println("✅ 用户A购买后，剩余库存: " + leftStock);

        // 3. 模拟超卖情况
        // 假设我们再疯狂减 10 次
        for (int i = 0; i < 10; i++) {
            leftStock = redisUtil.decrement(stockKey);
            if (leftStock < 0) {
                System.out.println("❌ 库存不足！当前库存: " + leftStock + " (回滚逻辑待实现)");
            } else {
                System.out.println("✅ 抢购成功，剩余: " + leftStock);
            }
        }
    }
}