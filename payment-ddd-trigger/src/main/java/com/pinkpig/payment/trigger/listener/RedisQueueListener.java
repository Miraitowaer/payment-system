package com.pinkpig.payment.trigger.listener;

import com.pinkpig.payment.domain.trade.model.dto.TradeOrderMsgDTO;
import com.pinkpig.payment.domain.trade.model.entity.TradeGoodsEntity;
import com.pinkpig.payment.domain.trade.model.entity.TradeOrderEntity;
import com.pinkpig.payment.domain.trade.repository.IGoodsRepository;
import com.pinkpig.payment.domain.trade.repository.ITradeRepository;
import com.pinkpig.payment.infrastructure.cache.RedisUtil;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 🎧 消费者：Redis 队列监听器
 * 系统启动后，它会自动运行，处理队列里的订单消息
 */
@Component
public class RedisQueueListener implements CommandLineRunner {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private IGoodsRepository goodsRepository;

    @Resource
    private ITradeRepository tradeRepository;

    @Resource
    private RedisUtil redisUtil;

    @Override
    public void run(String... args) throws Exception {
        // 启动一个独立的线程来监听，避免阻塞主线程
        new Thread(() -> {
            System.out.println("🎧 [消费者] Redis 队列监听器已启动，等待消息...");

            // 获取同一个队列 (Key 必须和生产者一致)
            RBlockingQueue<TradeOrderMsgDTO> queue = redissonClient.getBlockingQueue("trade_order_queue");

            while (true) {
                try {
                    // 🔄 take() 是阻塞方法：队列有货就拿，没货就睡，不耗 CPU
                    TradeOrderMsgDTO msg = queue.take();

                    System.out.println("⚙️ [消费者] 收到订单消息，准备落库: " + msg.getOrderId());

                    // --- 执行慢逻辑 (写数据库) ---
                    handleOrder(msg);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    System.err.println("❌ 处理消息异常: " + e.getMessage());
                    // 实际生产中可能需要把处理失败的消息扔到“死信队列”或者重试
                }
            }
        }).start();
    }

    /**
     * 处理订单落库逻辑
     */
    private void handleOrder(TradeOrderMsgDTO msg) {
        String stockKey = "goods:stock:" + msg.getProductId();

        // 0. 【前置检查】先查一下商品存不存在，价格是多少
        TradeGoodsEntity goods = goodsRepository.queryGoods(msg.getProductId());
//        System.out.println(goods);
        if (goods == null){
            redisUtil.increment(stockKey);
            throw new RuntimeException("商品不存在");
        }

        // 1. 扣减数据库库存 (这是真正的兜底扣减)
        boolean success = goodsRepository.deductStock(msg.getProductId());
        if (!success) {
            System.out.println("⚠️ 数据库库存不足 (极少见，因为Redis已经拦过了): " + msg.getOrderId());
            return;
        }

        // 2. 创建订单实体
        // 注意：价格等信息实际应该查商品表，这里为了演示简化写死或者假设 DTO 里有
        TradeOrderEntity newOrder = TradeOrderEntity.builder()
                .orderId(msg.getOrderId())
                .userId(msg.getUserId())
                .productId(msg.getProductId())
                .orderName(goods.getGoodsName()) // 简化
                .amount(new BigDecimal(goods.getPrice().toString())) // 简化
                .status("CREATE")
                .createTime(new Date())
                .build();

        // 3. 插入订单表
        tradeRepository.insert(newOrder);

        System.out.println("💾 [消费者] 订单落库成功！ID: " + msg.getOrderId());
    }
}