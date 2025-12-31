package com.pinkpig.payment.domain.trade.repository;

import com.pinkpig.payment.domain.trade.model.entity.TradeGoodsEntity;

public interface IGoodsRepository {
    // 查询商品信息
    TradeGoodsEntity queryGoods(String goodsId);

    // 重点：扣减库存
    // 返回值 int 代表受影响的行数：1表示扣减成功，0表示失败(库存不足)
    boolean deductStock(String goodsId);
}
