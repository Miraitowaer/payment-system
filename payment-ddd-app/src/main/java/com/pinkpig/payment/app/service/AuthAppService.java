package com.pinkpig.payment.app.service;

import com.pinkpig.payment.domain.auth.model.valobj.WeChatCallbackVO;
import com.pinkpig.payment.domain.auth.model.valobj.WeChatQrCodeValObj;
import com.pinkpig.payment.domain.auth.repository.ILoginRepository;
import com.pinkpig.payment.domain.auth.service.IWeChatService;
import com.pinkpig.payment.infrastructure.util.JwtUtil;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;

@Service // 交给 Spring 管理
public class AuthAppService {

    // 注入 Domain 层的接口
    // Spring 会自动找到 Infra 层那个写了 @Service 的实现类注入进来
    @Resource
    private IWeChatService weChatService;

    @Resource
    private ILoginRepository loginRepository; // 注入新定义的接口

    /**
     * 查询微信登录二维码
     */
    public WeChatQrCodeValObj queryWeChatLoginQrCode() {
        // 这里可以加日志、权限校验等非业务逻辑
        // System.out.println("应用层：开始调用领域服务...");

        return weChatService.getQrCodeTicket();
    }

    /**
     * 处理微信回调
     */
    public void receiveWeChatCallback(String xml) {
        // 1. 也是调用 Domain 接口解析
        WeChatCallbackVO callbackVO = weChatService.checkLogin(xml);
        if (callbackVO == null){
            System.out.println("收到非扫码事件回调，忽略处理");
            return;
        }
        // 我们还需要从 xml 解析出 Ticket (场景值/EventKey)
        // 注意：微信回调里的 EventKey，如果是扫码关注，前缀是 qrscene_，如果是已关注扫码，就是直接的数字/字符串
        // 这里为了演示简单，我们假设我们能解析出 ticket (你需要去 WeChatServiceImpl 的 checkLogin 里把 ticket 也返回出来，或者这里简单处理)

        // 【修正】：为了让你更顺畅，我们稍微修改一下 Domain 接口
        // 建议让 checkLogin 返回一个对象，包含 {openId, ticket}
        // 但这里我们简单点，假设我们解析出了 ticket
        String openId = callbackVO.getOpenId();
        String ticket = callbackVO.getTicket();

        // 2. 【关键】保存状态到 Redis
        if (openId != null && ticket != null) {
            loginRepository.saveLoginState(ticket, openId);
            System.out.println("状态已存入Redis，Ticket: " + ticket + ", OpenId: " + openId);
        }
    }

    // 记得导入 JwtUtil

    public String checkLoginState(String ticket) {
        String openId = loginRepository.checkLoginState(ticket);

        if (openId != null) {
            // 🎉 登录成功，生成 JWT 令牌
            // 以后前端就拿这个 Token 走遍天下
            return JwtUtil.createToken(openId);
        }
        return null;
    }
}