package com.pinkpig.payment.domain.trade.repository;

import com.pinkpig.payment.domain.trade.model.entity.TradeOrderEntity;

import java.util.List;

public interface ITradeRepository {

    /**
     * 查询是否存在未支付的订单 (防掉单核心)
     * @param userId 用户
     * @param productId 商品
     * @return 订单实体 (如果没查到返回 null)
     */
    TradeOrderEntity queryUnPayOrder(String userId, String productId);

    /**
     * 创建订单
     */
    void insert(TradeOrderEntity tradeOrderEntity);

    /**
     * 更新订单状态 (后续回调要用)
     */
    void updateStatus(String orderId, String status);

    /**
     * 查询所有处于某种状态，且创建时间早于某个时间的订单
     * @param status 订单状态
     * @return 订单列表
     */
    List<TradeOrderEntity> queryOrdersByStatus(String status);
}