package com.pinkpig.payment.trigger.http;

import com.pinkpig.payment.app.service.AuthAppService;
import com.pinkpig.payment.domain.auth.model.valobj.WeChatQrCodeValObj;
import com.pinkpig.payment.types.common.Response;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/v1/login/")
public class LoginController {

    @Resource
    private AuthAppService authAppService;

    /**
     * 接口：获取微信登录二维码
     * URL: http://localhost:8080/api/v1/login/weixin_code
     */
    @GetMapping("weixin_code")
    public Response<WeChatQrCodeValObj> getWeChatQrCode() {
        try {
            // 调用应用层
            WeChatQrCodeValObj data = authAppService.queryWeChatLoginQrCode();
            // 封装统一返回格式
            return Response.success(data);
        } catch (Exception e) {
            return Response.fail("500", "系统异常: " + e.getMessage());
        }
    }

    /**
     * 微信回调接口
     * 注意：这里本应该处理 GET (验签) 和 POST (接收消息)。
     * 为了简化，我们只写 POST，模拟接收扫码通知。
     */
    @RequestMapping("weixin_callback") // 微信服务器会调这个地址
    public String callback(@RequestBody String requestBody) {
        // requestBody 就是上面那段 XML
        System.out.println("收到微信回调: " + requestBody);

        // 调用应用层处理回调
        authAppService.receiveWeChatCallback(requestBody);

        // 按照微信要求，如果我们处理成功，要返回 "success"
        return "success";
    }

    /**
     * 前端轮询接口：检查是否登录成功
     * @param ticket 前端拿到的二维码凭证
     */
    @GetMapping("check_login")
    public Response<String> checkLogin(@RequestParam String ticket) {
        // 调 App Service
        String jwt = authAppService.checkLoginState(ticket);

        if (jwt != null) {
            // 登录成功！
            // 目前先返回 OpenID 证明成功
            return Response.success("登录成功，Token: " + jwt);
        } else {
            // 还没扫码，或者过期
            return Response.fail("0001", "等待扫码");
        }
    }

    /**
     * 测试接口：只有登录后带 Token 才能访问
     */
    @GetMapping("test_auth")
    public Response<String> testAuth(@RequestAttribute("openId") String openId) {
        // @RequestAttribute 是从拦截器里 setAttribute 取出来的
        return Response.success("你的身份是: " + openId);
    }
}