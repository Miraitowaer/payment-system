package com.pinkpig.payment.trigger.job;

import com.pinkpig.payment.domain.trade.model.entity.TradeOrderEntity;
import com.pinkpig.payment.domain.trade.repository.ITradeRepository;
import com.pinkpig.payment.infrastructure.gateway.AlipayStrategy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class OrderReplenishJob {

    @Resource
    private ITradeRepository tradeRepository;

    @Resource
    private AlipayStrategy alipayStrategy;

    /**
     * 每 30 秒执行一次
     * cron 表达式: 秒 分 时 日 月 周
     */
    @Scheduled(cron = "0/30 * * * * ?")
    public void exec() {
        System.out.println("⏰ 定时补单任务开始执行...");

        // 1. 捞出所有未支付的订单
        List<TradeOrderEntity> orderList = tradeRepository.queryOrdersByStatus("CREATE");
        if (orderList == null || orderList.isEmpty()) {
            System.out.println("没有未支付订单，休息一下。");
            return;
        }

        // 2. 逐个去支付宝查询
        for (TradeOrderEntity order : orderList) {
            String alipayStatus = alipayStrategy.queryOrder(order.getOrderId());
            System.out.println("查询订单: " + order.getOrderId() + "，支付宝状态: " + alipayStatus);

            // 3. 如果支付宝说已经付了，我们就同步更新数据库
            if ("TRADE_SUCCESS".equals(alipayStatus)) {
                tradeRepository.updateStatus(order.getOrderId(), "PAID");
                System.out.println("✅ 订单 " + order.getOrderId() + " 同步支付成功！");
            }
        }
    }
}