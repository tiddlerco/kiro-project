package com.example.patterns.behavioral.observer;

import com.example.patterns.behavioral.observer.domain.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 积分发放监听者。
 *
 * <p>观察者模式中的「具体观察者（ConcreteObserver）」角色之一，订阅 {@link OrderStatusChangedEvent}，
 * 在订单状态变更后按业务规则为买家发放积分。它与 {@link SmsNotifyListener} 同时监听同一事件，
 * 但二者关注点不同、彼此独立、互不引用，共同体现「同一事件可被多个相互独立的监听者各自处理」
 * 的观察者模式价值（对应需求 4.3）。</p>
 *
 * <p>本监听者只承担「发放积分」这一关注点，并自行决定对哪些状态变更发放积分：仅当订单进入
 * 「已支付」或「已完成」时才发放，其余状态变更不发放。该业务判断完全内聚于本监听者，
 * 既不影响发布方，也不影响其它监听者。</p>
 *
 * @since 1.0.0
 */
@Component
public class PointsRewardListener {

    /** 日志记录器 */
    private static final Logger log = LoggerFactory.getLogger(PointsRewardListener.class);

    /** 订单支付成功后发放的积分数。 */
    private static final int POINTS_ON_PAID = 100;

    /** 订单交易完成后发放的积分数。 */
    private static final int POINTS_ON_COMPLETED = 50;

    /** 无需发放积分时的积分数。 */
    private static final int NO_POINTS = 0;

    /**
     * 监听订单状态变更事件并按规则发放积分。
     *
     * <p>由 Spring 的 {@link EventListener} 机制在事件发布后自动回调。本方法根据事件中的新状态
     * 计算应发放的积分，仅在积分大于 0 时执行发放（此处以日志模拟发放结果，作为可观察的执行
     * 结果）；不满足发放条件的状态变更则跳过，不产生积分。</p>
     *
     * @param event 订单状态变更事件
     */
    @EventListener
    public void grantRewardPoints(OrderStatusChangedEvent event) {
        int points = calculateRewardPoints(event.getNewStatus());
        if (points <= NO_POINTS) {
            log.info("[积分发放] 订单[{}]变更为「{}」无需发放积分。",
                    event.getOrderNo(), event.getNewStatus().getDescription());
            return;
        }
        log.info("[积分发放] 向订单[{}]买家发放积分 {} 分（状态变更为「{}」）。",
                event.getOrderNo(), points, event.getNewStatus().getDescription());
    }

    /**
     * 依据订单新状态计算应发放的积分。
     *
     * <p>业务规则：订单进入「已支付」发放 {@value #POINTS_ON_PAID} 积分，进入「已完成」发放
     * {@value #POINTS_ON_COMPLETED} 积分，其余状态不发放积分（返回 {@value #NO_POINTS}）。</p>
     *
     * @param newStatus 变更后的订单状态
     * @return 应发放的积分数；无需发放时返回 0
     */
    private int calculateRewardPoints(OrderStatus newStatus) {
        if (OrderStatus.PAID == newStatus) {
            return POINTS_ON_PAID;
        }
        if (OrderStatus.COMPLETED == newStatus) {
            return POINTS_ON_COMPLETED;
        }
        return NO_POINTS;
    }
}
