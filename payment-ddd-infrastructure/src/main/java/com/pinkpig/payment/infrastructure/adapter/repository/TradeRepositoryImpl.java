package com.pinkpig.payment.infrastructure.adapter.repository;

import com.pinkpig.payment.domain.trade.model.entity.TradeOrderEntity;
import com.pinkpig.payment.domain.trade.repository.ITradeRepository;
import com.pinkpig.payment.infrastructure.dao.TradeOrderMapper;
import org.springframework.stereotype.Repository;
import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Repository
public class TradeRepositoryImpl implements ITradeRepository {

    @Resource
    private TradeOrderMapper tradeOrderMapper;

    @Override
    public TradeOrderEntity queryUnPayOrder(String userId, String productId) {
        // 直接调用 MyBatis Mapper
        return tradeOrderMapper.queryUnPayOrder(userId, productId);
    }

    @Override
    public void insert(TradeOrderEntity tradeOrderEntity) {
        tradeOrderMapper.insert(tradeOrderEntity);
    }

    @Override
    public void updateStatus(String orderId, String status) {
        tradeOrderMapper.updateStatus(orderId, status, new Date());
    }

    @Override
    public List<TradeOrderEntity> queryOrdersByStatus(String status) {
        return tradeOrderMapper.queryOrdersByStatus(status);
    }
}