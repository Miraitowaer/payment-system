package com.pinkpig.payment.domain.trade.model.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TradeGoodsEntity {
    private Long id;
    private String goodsId;
    private String goodsName;
    private BigDecimal price;
    private Integer stock;
}
