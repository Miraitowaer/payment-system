package com.pinkpig.payment.infrastructure.adapter.repository;

import com.pinkpig.payment.domain.trade.model.entity.TradeGoodsEntity;
import com.pinkpig.payment.domain.trade.repository.IGoodsRepository;
import com.pinkpig.payment.infrastructure.dao.TradeGoodsMapper;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class GoodsRepositoryImpl implements IGoodsRepository{
    @Resource
    private TradeGoodsMapper tradeGoodsMapper;
    @Override
    public TradeGoodsEntity queryGoods(String goodsId) {
        return tradeGoodsMapper.queryGoods(goodsId);
    }

    @Override
    public boolean deductStock(String goodsId) {
        int rows = tradeGoodsMapper.deductStock(goodsId);
        return rows > 0;
    }
}
