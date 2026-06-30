package com.example.patterns.structural.decorator;

import com.example.patterns.structural.decorator.domain.NotifyContent;
import com.example.patterns.structural.decorator.domain.SendResult;
import org.springframework.stereotype.Component;

/**
 * 基础通知发送器。
 *
 * <p>装饰器模式中的「具体组件（ConcreteComponent）」角色，承担装饰链最内层的真实发送逻辑：
 * 将通知内容原样发出，返回一个不含任何增强能力的基础成功结果。各装饰器在此结果之上叠加
 * 签名、加密、日志等增强能力。</p>
 *
 * <p>本类以 {@link Component} 交由 Spring 容器管理，作为装饰链最内层的基础组件供服务层注入
 * 后按需包装（对应需求 10.1：协作角色由容器注入而非内部 new）。</p>
 *
 * @since 1.0.0
 */
@Component
public class BaseNotifySender implements NotifySender {

    /**
     * 执行基础通知发送。
     *
     * <p>作为装饰链最内层的真实发送动作：以传入正文作为最终发送内容，构造一个已应用能力为空
     * 的基础成功结果，交由外层各装饰器继续叠加增强能力。</p>
     *
     * @param content 通知内容，承载接收人、标题与正文
     * @return 基础发送成功的结果，最终正文即为传入的正文，已应用能力列表为空
     */
    @Override
    public SendResult send(NotifyContent content) {
        return SendResult.success(content.getReceiver(), content.getBody());
    }
}
