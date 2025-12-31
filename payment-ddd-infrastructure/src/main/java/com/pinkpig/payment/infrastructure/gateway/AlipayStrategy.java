package com.pinkpig.payment.infrastructure.gateway;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.pinkpig.payment.infrastructure.config.AlipayConfigProperties;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class AlipayStrategy {

    @Resource
    private AlipayConfigProperties alipayConfig;

    /**
     * 调用支付宝接口，生成支付表单 (HTML)
     * @param orderId 我们的订单号
     * @param amount  金额
     * @param subject 商品名称
     * @return 支付宝返回的 HTML Form 表单字符串
     */
    public String doPay(String orderId, String amount, String subject) {
        try {
            // 1. 初始化支付宝客户端 (相当于拿好钥匙和印章)
            AlipayClient alipayClient = new DefaultAlipayClient(
                    alipayConfig.getGatewayUrl(),
                    alipayConfig.getAppId(),
                    alipayConfig.getMerchantPrivateKey(),
                    "json",
                    "UTF-8",
                    alipayConfig.getAlipayPublicKey(),
                    "RSA2" // 签名算法
            );

            // 2. 组装请求参数
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            request.setNotifyUrl(alipayConfig.getNotifyUrl()); // 异步回调地址(后端用)
            request.setReturnUrl(alipayConfig.getReturnUrl()); // 同步跳转地址(前端用)

            // 3. 组装业务数据 (JSON格式)
            JSONObject bizContent = new JSONObject();
            bizContent.put("out_trade_no", orderId);       // 商户订单号 (必填)
            bizContent.put("total_amount", amount);        // 订单金额 (必填)
            bizContent.put("subject", subject);            // 订单标题 (必填)
            bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY"); // 电脑网站支付固定码

            request.setBizContent(bizContent.toString());

            // 4. 发起请求 (获得一个 HTML 字符串)
            // pageExecute 会生成一个包含 auto-submit form 的 HTML
            String form = alipayClient.pageExecute(request).getBody();

            return form;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("调用支付宝失败");
        }
    }

    /**
     * 查询订单支付状态
     * @param orderId 商户订单号
     * @return 支付宝返回的查询结果（包含交易状态）
     */
    public String queryOrder(String orderId) {
        try {
            AlipayClient alipayClient = new DefaultAlipayClient(
                    alipayConfig.getGatewayUrl(),
                    alipayConfig.getAppId(),
                    alipayConfig.getMerchantPrivateKey(),
                    "json",
                    "UTF-8",
                    alipayConfig.getAlipayPublicKey(),
                    "RSA2"
            );

            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            JSONObject bizContent = new JSONObject();
            bizContent.put("out_trade_no", orderId);
            request.setBizContent(bizContent.toString());

            // 发起查询
            AlipayTradeQueryResponse response = alipayClient.execute(request);

            if (response.isSuccess()) {
                // 返回交易状态，如 TRADE_SUCCESS, WAIT_BUYER_PAY
                return response.getTradeStatus();
            } else {
                // 比如查无此单，或者调用失败
                return "UNKNOWN";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR";
        }
    }
}