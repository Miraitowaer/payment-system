package com.pinkpig.payment.app.service;

import cn.hutool.core.util.IdUtil;
import com.pinkpig.payment.domain.trade.model.entity.TradeGoodsEntity;
import com.pinkpig.payment.domain.trade.model.entity.TradeOrderEntity;
import com.pinkpig.payment.domain.trade.repository.IGoodsRepository;
import com.pinkpig.payment.domain.trade.repository.ITradeRepository;
import com.pinkpig.payment.infrastructure.cache.RedisUtil;
import com.pinkpig.payment.infrastructure.gateway.AlipayStrategy;
import org.apache.commons.lang3.RandomStringUtils;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Service
public class TradeAppService {

    @Resource
    private ITradeRepository tradeRepository;

    @Resource
    private AlipayStrategy alipayStrategy;

    @Resource
    private IGoodsRepository goodsRepository;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private RedissonClient redissonClient; // 注入 Redisson

    /**
     * 创建交易订单 (核心流程)
     * @param userId 用户OpenID
     * @param productId 商品ID
     * @return 支付参数 (比如支付宝的 Form 表单，这里暂时返回 orderId)
     */
    public String createOrder(String userId, String productId) {

        String stockKey = "goods:stock:" + productId;

        // 1. 原子递减
//        Long currentStock = redisUtil.decrement(stockKey);

        boolean isSuccess = redisUtil.deductStockLua(stockKey);

        // 2. 判定结果
        if (!isSuccess) {
            // 如果减完是 -1，说明刚才已经是 0 了，库存不足
            // 此时流量被拦截在 Redis 层，根本不会去查数据库，保护了 DB
            System.out.println("⛔️ Redis 拦截：库存不足！User: " + userId);
            throw new RuntimeException("手慢了，已被抢光！(Redis)");
        }

        // 如果代码走到这里，说明 Redis 里抢到了名额 (stock >= 0)
        // 接下来才允许去数据库里真的下单

        // 0. 【前置检查】先查一下商品存不存在，价格是多少
        TradeGoodsEntity goods = goodsRepository.queryGoods(productId);
//        System.out.println(goods);
        if (goods == null){
            redisUtil.increment(stockKey);
            throw new RuntimeException("商品不存在");
        }

        // 1. 【防掉单检查】
        // 查询该用户对该商品，是否有一笔没支付的烂账？
        TradeOrderEntity existOrder = tradeRepository.queryUnPayOrder(userId, productId);

        if (existOrder != null) {
            System.out.println("检测到掉单(未支付订单)，直接复用，订单号: " + existOrder.getOrderId());
            // 直接返回旧订单号，不去创建新的
            // 如果是复用旧单，说明不是新抢购，要把刚才 Redis 扣掉的名额还回去！
            redisUtil.increment(stockKey);
            return alipayStrategy.doPay(existOrder.getOrderId(), existOrder.getAmount().toString(), existOrder.getOrderName());
        }

        // 2. 【扣减库存】
        boolean success = goodsRepository.deductStock(productId);
        if (!success){
            redisUtil.increment(stockKey);
            System.out.println("库存不足，抢购失败！用户：" + userId);
            throw new RuntimeException("手慢了，库存不足！");
        }

        // 3. 【创建新订单】
        // 如果没有掉单，说明是全新的购买请求
        TradeOrderEntity newOrder = TradeOrderEntity.builder()
                .orderId(generateOrderId()) // 生成雪花ID
                .userId(userId)
                .productId(productId)
                .orderName(goods.getGoodsName()) // 实际应该查商品表
                .amount(new BigDecimal(goods.getPrice().toString())) // 实际应该查商品表
                .status("CREATE")
                .createTime(new Date())
                .build();

        // 3. 落库 (如果此时用户并发点了两次，数据库唯一索引会在这里抛异常，实现兜底防重)
//        System.out.println(newOrder);
        tradeRepository.insert(newOrder);
        System.out.println("创建新订单成功，订单号: " + newOrder.getOrderId());

        // 4. 调用支付域能力
        String orderIdToPay = (existOrder != null) ? existOrder.getOrderId() : newOrder.getOrderId();

        return alipayStrategy.doPay(orderIdToPay, newOrder.getAmount().toString(), newOrder.getOrderName());
    }

    /**
     * 模拟简单的订单号生成 (实习阶段用 UUID + 时间戳，生产环境用雪花算法)
     */
//    private String generateOrderId() {
//        return System.currentTimeMillis() + RandomStringUtils.randomNumeric(4);
//    }
    private String generateOrderId() {
        // 参数1: workerId (机器ID，0-31)
        // 参数2: datacenterId (数据中心ID，0-31)

        // 🔍 知识点：
        // 在真实生产环境（多台服务器集群），每台服务器的这两个参数必须不同！
        // 通常是读取配置文件 (application.yml) 或者由运维脚本注入。
        // 但因为你现在是单机实习开发，直接写死 (1, 1) 没问题。

        // nextIdStr() 会返回一个字符串类型的长数字
        return IdUtil.getSnowflake(1, 1).nextIdStr();
    }

    private String doPrepay(String orderId, BigDecimal amount) {
        // 这里将来要调支付宝
        return "即将跳转支付宝... OrderId: " + orderId;
    }
}