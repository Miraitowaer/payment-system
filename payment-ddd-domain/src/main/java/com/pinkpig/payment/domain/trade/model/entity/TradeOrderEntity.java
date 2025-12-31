package com.pinkpig.payment.domain.trade.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeOrderEntity {

    /** 自增主键 */
    private Long id;

    /** 订单号 (业务主键) */
    private String orderId;

    /** 用户ID */
    private String userId;

    /** 商品ID */
    private String productId;

    /** 订单名称 */
    private String orderName;

    /** 金额 */
    private BigDecimal amount;

    /** * 订单状态
     * CREATE(创建/待支付), PAYING(支付中), SUCCESS(支付成功), FAIL(失败/关闭)
     */
    private String status;

    /** 创建时间 */
    private Date createTime;

    // --- 业务方法 ---
    // 比如：判断是否未支付
    public boolean isUnPay() {
        return "CREATE".equals(this.status) || "PAYING".equals(this.status);
    }
}