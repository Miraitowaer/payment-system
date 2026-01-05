package com.pinkpig.payment.trigger;

import com.pinkpig.payment.PaymentApplication;
import com.pinkpig.payment.app.service.TradeAppService;
import com.pinkpig.payment.infrastructure.cache.RedisUtil;
import com.pinkpig.payment.infrastructure.gateway.AlipayStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.anyString;

// ✅ 这里直接用 PaymentApplication 启动，模拟完整的生产环境
@SpringBootTest(classes = PaymentApplication.class)
public class RealAttackTest {

    @Resource
    private TradeAppService tradeAppService;

    @Resource
    private RedisUtil redisUtil;

    // 🕵️‍♂️ 关键点：Mock 掉支付宝，不让它真的去调 API，只验证前面的库存逻辑
    @MockBean
    private AlipayStrategy alipayStrategy;

    @BeforeEach
    public void init() {
        // 定义 Mock 行为：只要调用 doPay，就返回一个假表单，不报错
        Mockito.when(alipayStrategy.doPay(anyString(), anyString(), anyString()))
                .thenReturn("<html>Mock支付宝表单</html>");

        // 1. 重置 Redis 库存 (模拟 100 个库存)
        // ⚠️ 确保你的数据库里 trade_goods 表 goods_id='1002' 的 stock 也是 100！要同步！
        redisUtil.set("goods:stock:1002", "100");
    }

    @Test
    public void attackCreateOrder() throws InterruptedException {
        int userCount = 1000; // 1000 个用户
        ExecutorService executorService = Executors.newFixedThreadPool(userCount);
        CountDownLatch startGun = new CountDownLatch(1);
        CountDownLatch finishLine = new CountDownLatch(userCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < userCount; i++) {
            // 每个线程代表一个不同的用户
            String userId = "user_" + i;

            executorService.submit(() -> {
                try {
                    startGun.await(); // 等枪响

                    // 🔥🔥 真正的全链路攻击！调用 Service 层 🔥🔥
                    // 如果抢到了，会返回表单字符串；如果没抢到，会抛 RuntimeException
                    tradeAppService.createOrder(userId, "1002");

                    // 能走到这里没报错，说明抢购成功
                    successCount.incrementAndGet();

                } catch (Exception e) {
                    // 捕获异常（"手慢了"、"库存不足"等）
                    failCount.incrementAndGet();
                    // System.out.println(e.getMessage()); // 可选：打印失败原因
                } finally {
                    finishLine.countDown();
                }
            });
        }

        // 预备...跑！
        Thread.sleep(1000);
        System.out.println("🚀 1000 人并发下单开始...");
        startGun.countDown();
        finishLine.await(); // 等所有人跑完

        System.out.println("=======================================");
        System.out.println("📊 真实下单压测结果：");
        System.out.println("商品总数：100");
        System.out.println("参与人数：1000");
        System.out.println("✅ 下单成功：" + successCount.get());
        System.out.println("❌ 下单失败：" + failCount.get());

        // 验证 Redis 最终状态
        String finalStock = redisUtil.get("goods:stock:1002");
        System.out.println("📝 Redis 剩余库存: " + finalStock);
        System.out.println("=======================================");
    }
}