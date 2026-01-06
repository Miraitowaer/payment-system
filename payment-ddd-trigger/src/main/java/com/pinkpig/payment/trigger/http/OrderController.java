package com.pinkpig.payment.trigger.http;

import com.pinkpig.payment.app.service.TradeAppService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/api/v1/order")
public class OrderController {

    @Resource
    private TradeAppService tradeAppService;

    /**
     * 下单接口
     * 1. 验证用户 (拦截器已做)
     * 2. 创建订单
     * 3. 返回支付宝 Form 表单 HTML
     */
    @PostMapping(value = "/create", produces = "text/html")
    public void createOrder(@RequestAttribute("openId") String openId,
                            HttpServletResponse response) throws IOException {

        // 这里的 productId 先写死 "1001" (我们数据库里只有这一个商品)
        // 以后真正做电商时，这个 ID 是前端传过来的
        String formStr = tradeAppService.createOrder(openId, "1006");

        // 直接把 HTML 写回给浏览器，浏览器收到后会自动渲染并跳转
        response.setContentType("text/html;charset=utf-8");
        response.getWriter().write(formStr);
        response.getWriter().flush();
        response.getWriter().close();
    }
}