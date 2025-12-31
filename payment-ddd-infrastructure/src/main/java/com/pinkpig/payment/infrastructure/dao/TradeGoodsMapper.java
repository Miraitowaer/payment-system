package com.pinkpig.payment.infrastructure.dao;

import com.pinkpig.payment.domain.trade.model.entity.TradeGoodsEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TradeGoodsMapper {
    // 查询商品信息
    TradeGoodsEntity queryGoods(@Param("goodsId") String goodsId);

    // 重点：扣减库存
    // 返回值 int 代表受影响的行数：1表示扣减成功，0表示失败(库存不足)
    int deductStock(@Param("goodsId") String goodsId);
}
