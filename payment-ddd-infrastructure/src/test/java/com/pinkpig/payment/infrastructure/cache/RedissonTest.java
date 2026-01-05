package com.pinkpig.payment.infrastructure.cache;

import com.pinkpig.payment.infrastructure.TestApplication;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@SpringBootTest(classes = TestApplication.class)
public class RedissonTest {

    @Resource
    private RedissonClient redissonClient; // 👈 注入 Redisson 客户端

    @Test
    public void testWatchDog() throws InterruptedException {
        // 1. 获取一把锁 (只要名字一样，就是同一把锁)
        RLock lock = redissonClient.getLock("any-lock");

        // 2. 加锁
        // ⚠️ 重点：如果不传过期时间，默认就是 30秒，且会启动【看门狗】自动续期！
        // 如果你写了 lock.lock(10, TimeUnit.SECONDS)，看门狗就不工作了！
        lock.lock();

        System.out.println("🔒 加锁成功！开始执行长耗时业务...");

        // 3. 开启一个子线程，每秒打印一次锁的剩余时间 (TTL)
        new Thread(() -> {
            try {
                while (true) {
                    // ttl 是“Time To Live”，即锁还能活多久
                    long ttl = lock.remainTimeToLive();
                    System.out.println("⏳ 锁剩余生存时间: " + ttl + "ms");
                    Thread.sleep(2000);
                }
            } catch (Exception e) {}
        }).start();

        // 4. 模拟超长业务：本来锁只有30秒，但我睡 60秒！
        // 如果没有看门狗，30秒后锁就没了。
        // 有了看门狗，你会发现 ttl 变少后，又突然变回 30秒！
        Thread.sleep(60000);

        // 5. 释放锁
        lock.unlock();
        System.out.println("🔓 业务结束，释放锁");
    }
}