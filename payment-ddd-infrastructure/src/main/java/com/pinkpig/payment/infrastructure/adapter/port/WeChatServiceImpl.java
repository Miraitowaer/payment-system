package com.pinkpig.payment.infrastructure.adapter.port;

import com.pinkpig.payment.domain.auth.model.valobj.WeChatCallbackVO;
import com.pinkpig.payment.domain.auth.model.valobj.WeChatQrCodeValObj;
import com.pinkpig.payment.domain.auth.service.IWeChatService;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class WeChatServiceImpl implements IWeChatService {

    // 注入刚才我们在 Config 类里通过 @Bean 创建的对象
    @Resource
    private WxMpService wxMpService;

    @Override
    public WeChatQrCodeValObj getQrCodeTicket() {
        try {
            // 1. 调用微信接口生成二维码 Ticket
            // "1001" 是场景值(sceneId)，我们可以用来区分是哪个用户扫的，暂时写死
            // 600 是过期时间(秒)
            WxMpQrCodeTicket ticket = wxMpService.getQrcodeService().qrCodeCreateTmpTicket(1001, 600);

            // 2. 封装返回 Domain 层的对象
            return WeChatQrCodeValObj.builder()
                    .ticket(ticket.getTicket())
                    .codeUrl(ticket.getUrl()) // 这是解析后的短链接
                    .expireSeconds(ticket.getExpireSeconds())
                    .build();

        } catch (Exception e) {
            // 实际生产中这里应该打印日志 log.error(...)
            throw new RuntimeException("调用微信接口失败: " + e.getMessage());
        }
    }

    @Override
    public WeChatCallbackVO checkLogin(String callbackXml) {
        try {
            // 使用 WxJava 提供的工具类解析 XML
            WxMpXmlMessage message = WxMpXmlMessage.fromXml(callbackXml);

            // 获取 OpenId
            String openId = message.getFromUser();
            String ticket = message.getTicket();
            String eventKey = message.getEventKey(); // 场景值
            String event = message.getEvent();

            // 这里可以做一些逻辑判断，比如必须是 SCAN 事件
            if ("SCAN".equals(message.getEvent()) || "subscribe".equals(message.getEvent())) {
                return WeChatCallbackVO.builder()
                        .openId(openId)
                        .ticket(ticket)
                        .eventKey(eventKey)
                        .build();
            }

            return null;
        } catch (Exception e) {
            throw new RuntimeException("XML解析失败", e);
        }
    }
}