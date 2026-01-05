package com.pinkpig.payment.domain.trade.model.dto; // 找个合适的地方放

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

// 必须实现 Serializable，因为要网络传输存进 Redis
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TradeOrderMsgDTO implements Serializable {
    private String userId;
    private String productId;
    private String orderId; // 我们可以提前生成好订单号
}