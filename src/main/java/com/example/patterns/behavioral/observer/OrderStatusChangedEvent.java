package com.example.patterns.behavioral.observer;

import com.example.patterns.behavioral.observer.domain.OrderStatus;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 订单状态变更事件。
 *
 * <p>观察者模式中的「事件（被观察主题对外发布的消息）」角色，承载一次订单状态变更的完整快照：
 * 订单号、变更前状态、变更后状态、操作人与变更时间。它由 {@link OrderStatusEventPublisher}
 * 发布，并被 {@link SmsNotifyListener}、{@link PointsRewardListener} 等多个相互独立的监听者
 * 各自消费，从而实现发布方与监听方的彻底解耦（对应需求 4.3、10.3）。</p>
 *
 * <p>本事件刻意设计为不依赖 Spring 框架的普通 POJO（不继承 {@code ApplicationEvent}）：
 * 自 Spring 4.2 起 {@code ApplicationEventPublisher} 支持发布任意对象作为事件，使事件模型
 * 与框架解耦、可被独立测试与复用，更契合「面向抽象、降低耦合」的设计取向。</p>
 *
 * <p>事件对象一经创建即不可变（全部字段 {@code final}、仅提供读取访问），以保证同一事件
 * 被多个监听者消费时不会因任一监听者的修改而相互影响，符合「领域事件应为不可变快照」的实践；
 * 统一通过 {@link #of} 静态工厂方法创建。</p>
 *
 * @since 1.0.0
 */
@Getter
public class OrderStatusChangedEvent {

    /** 发生状态变更的订单号。 */
    private final String orderNo;

    /** 变更前的订单状态。 */
    private final OrderStatus oldStatus;

    /** 变更后的订单状态。 */
    private final OrderStatus newStatus;

    /** 触发本次状态变更的操作人标识（如客服工号、系统任务名）。 */
    private final String operator;

    /** 状态变更发生的时间。 */
    private final LocalDateTime changeTime;

    /**
     * 全参构造订单状态变更事件。
     *
     * <p>设为私有以强制经由 {@link #of} 静态工厂创建，从而集中填充变更时间并保持创建入口唯一。</p>
     *
     * @param orderNo    发生状态变更的订单号
     * @param oldStatus  变更前的订单状态
     * @param newStatus  变更后的订单状态
     * @param operator   触发本次变更的操作人标识
     * @param changeTime 状态变更发生的时间
     */
    private OrderStatusChangedEvent(String orderNo, OrderStatus oldStatus, OrderStatus newStatus,
                                    String operator, LocalDateTime changeTime) {
        this.orderNo = orderNo;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.operator = operator;
        this.changeTime = changeTime;
    }

    /**
     * 创建一个订单状态变更事件，并将变更时间默认填充为当前时刻。
     *
     * @param orderNo   发生状态变更的订单号
     * @param oldStatus 变更前的订单状态
     * @param newStatus 变更后的订单状态
     * @param operator  触发本次变更的操作人标识
     * @return 字段填充完成且不可变的订单状态变更事件
     */
    public static OrderStatusChangedEvent of(String orderNo, OrderStatus oldStatus,
                                             OrderStatus newStatus, String operator) {
        return new OrderStatusChangedEvent(orderNo, oldStatus, newStatus, operator, LocalDateTime.now());
    }
}
