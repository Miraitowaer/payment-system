package com.pinkpig.payment.infrastructure.cache;

import com.pinkpig.payment.infrastructure.TestApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest(classes = TestApplication.class)
public class ConcurrencyTest {

    @Resource
    private RedisUtil redisUtil;

    @Test
    public void benchmark() throws InterruptedException {
        // 1. 初始化库存：假设有 100 个 iPhone
        String stockKey = "goods:stock:1004";
        redisUtil.set(stockKey, "100");
        System.out.println("🔥 预热完成，初始库存：100");

        // 2. 模拟 1000 人同时抢购
        int userCount = 1000;
        // 创建一个能容纳 1000 个线程的线程池
        ExecutorService executorService = Executors.newFixedThreadPool(userCount);

        // 发令枪：初始值为 1，代表“门是关着的”
        CountDownLatch startGun = new CountDownLatch(1);
        CountDownLatch finishLine = new CountDownLatch(userCount); // 终点线 (1000个人都要冲线)

        // 计数器：统计有多少人抢到了，有多少人被挡回去了
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // 3. 开始任务分配
        for (int i = 0; i < userCount; i++) {
            executorService.submit(() -> {
                try {
                    // 所有线程都会阻塞在这里，等待发令枪响
                    startGun.await();

                    // --- 🚀 真正的并发时刻 ---
                    long startTime = System.currentTimeMillis();

                    // 抢购逻辑
//                    Long stock = redisUtil.decrement(stockKey);

                    boolean success = redisUtil.deductStockLua(stockKey);

                    if (success) {
                        successCount.incrementAndGet();
                        // 打印日志感受速度 (只打印成功的，不然控制台刷屏太快)
                        System.out.println("✅ 抢到了! (Lua) | 耗时: " + (System.currentTimeMillis() - startTime) + "ms");
                    } else {
                        failCount.incrementAndGet();
                        // System.out.println("⛔️ 被 Redis 拦截"); // 失败的太多了，先注释掉，免得看不过来
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    finishLine.countDown(); //跑完了一定要报告！(不管成功失败)
                }
            });
        }

        // 等待子线程跑完（这里简单休眠一下，实际可以用另一个Latch控制结束）
        Thread.sleep(3000);

        System.out.println("预备...... 砰！");
// 扣动扳机：把数字减为 0，那 1000 个卡在 await() 的线程瞬间解冻
        startGun.countDown();

        finishLine.await();  //主线程在此死等，直到 1000 个人都跑过终点线

        System.out.println("=======================================");
        System.out.println("📊 最终结果统计：");
        System.out.println("初始库存：100");
        System.out.println("参与人数：1000");
        System.out.println("✅ 抢购成功：" + successCount.get() + " 人 (预期应为 100)");
        System.out.println("⛔️ 拦截失败：" + failCount.get() + " 人 (预期应为 900)");
        System.out.println("=======================================");
    }
}