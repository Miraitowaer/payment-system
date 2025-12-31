package com.pinkpig.payment.domain.auth.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 微信回调值对象
 * 作用：封装从微信 XML 中解析出来的核心数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeChatCallbackVO {

    /**
     * 用户 OpenId (谁扫的)
     */
    private String openId;

    /**
     * 二维码 Ticket (扫的哪个码)
     * 前端轮询状态时，是用 Ticket 来查的，所以这个必须解析出来
     */
    private String ticket;

    /**
     * 场景值 (EventKey)
     * 比如我们之前传的 "1001"，业务逻辑可能需要用它来判断是登录还是绑定
     */
    private String eventKey;
}