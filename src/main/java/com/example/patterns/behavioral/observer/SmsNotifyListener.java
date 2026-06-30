package com.example.patterns.behavioral.observer;

import com.example.patterns.behavioral.observer.domain.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 短信通知监听者。
 *
 * <p>观察者模式中的「具体观察者（ConcreteObserver）」角色之一，订阅 {@link OrderStatusChangedEvent}，
 * 在订单状态变更后向买家发送短信通知。它仅依赖事件本身，既不感知发布者，也不感知
 * {@link PointsRewardListener} 等其它监听者的存在，与各方完全解耦。</p>
 *
 * <p>本监听者只承担「发短信通知」这一关注点，与「发放积分」等其它关注点彼此独立：即便本监听者
 * 处理失败或被移除，也不影响其它监听者对同一事件的处理（体现观察者模式「同一事件、多个相互
 * 独立的监听者各自处理」的解耦价值，对应需求 4.3）。</p>
 *
 * @since 1.0.0
 */
@Component
public class SmsNotifyListener {

    /** 日志记录器 */
    private static final Logger log = LoggerFactory.getLogger(SmsNotifyListener.class);

    /**
     * 监听订单状态变更事件并发送短信通知。
     *
     * <p>由 Spring 的 {@link EventListener} 机制在事件发布后自动回调。本方法依据事件中的新状态
     * 构造面向买家的短信文案并「发送」（此处以日志模拟发送结果，作为可观察的执行结果）。</p>
     *
     * @param event 订单状态变更事件
     */
    @EventListener
    public void sendNotificationSms(OrderStatusChangedEvent event) {
        String content = buildSmsContent(event);
        log.info("[短信通知] 向订单[{}]买家发送短信：{}", event.getOrderNo(), content);
    }

    /**
     * 依据订单新状态构造短信通知文案。
     *
     * @param event 订单状态变更事件
     * @return 面向买家的短信通知文案
     */
    private String buildSmsContent(OrderStatusChangedEvent event) {
        OrderStatus newStatus = event.getNewStatus();
        return String.format("您的订单%s状态已更新为「%s」，感谢您的惠顾。",
                event.getOrderNo(), newStatus.getDescription());
    }
}
