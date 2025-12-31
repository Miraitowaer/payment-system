package com.pinkpig.payment.infrastructure.dao;

import com.pinkpig.payment.domain.trade.model.entity.TradeOrderEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import javax.xml.crypto.Data;
import java.util.Date;
import java.util.List;

@Mapper
public interface TradeOrderMapper {

    // 对应 ITradeRepository.insert
    void insert(TradeOrderEntity entity);

    // 对应 ITradeRepository.queryUnPayOrder
    // 只要一条，所以返回对象而不是List
    TradeOrderEntity queryUnPayOrder(@Param("userId") String userId,
                                     @Param("productId") String productId);

    // 查询所有处于某种状态，且创建时间早于某个时间的订单
    List<TradeOrderEntity> queryOrdersByStatus(@Param("status") String status);

    //更新订单状态
    void updateStatus(@Param("orderId") String orderId,
                      @Param("status") String status,
                      @Param("payTime") Date payTime);
}