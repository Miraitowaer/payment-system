package com.pinkpig.payment.infrastructure.cache;

import com.pinkpig.payment.infrastructure.TestApplication;
import org.junit.jupiter.api.Test;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RFuture;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.concurrent.ExecutionException;

@SpringBootTest(classes = TestApplication.class)
public class AsyncDemoTest {

    @Resource
    private RedissonClient redissonClient;

    @Test
    public void testAsyncVsSync() throws ExecutionException, InterruptedException {
        RBlockingQueue<String> queue = redissonClient.getBlockingQueue("test_speed_queue");

        // ==========================================
        // 1. 同步发送 (Sync)
        // ==========================================
        long start1 = System.nanoTime();
        // 这一行代码执行完，意味着消息【一定】已经到达 Redis 了
        queue.offer("同步消息");
        long end1 = System.nanoTime();
        System.out.println("🐢 同步发送耗时: " + (end1 - start1) / 1000 + " 微秒 (等待网络响应)");

        // ==========================================
        // 2. 异步发送 (Async)
        // ==========================================
        long start2 = System.nanoTime();
        // 这一行代码执行完，消息可能还在本地网卡里，还没发出去呢，主线程就已经往下走了
        RFuture<Boolean> future = queue.offerAsync("异步消息");
        long end2 = System.nanoTime();
        System.out.println("🚀 异步发送耗时: " + (end2 - start2) / 1000 + " 微秒 (不等待，直接走)");

        // 验证一下结果 (这一步是多余的，只是为了证明它确实发出去了)
        // 我们可以等它真正完成
        future.get(); // 阻塞直到完成
        System.out.println("✅ 异步消息确认已到达 Redis");
    }
}