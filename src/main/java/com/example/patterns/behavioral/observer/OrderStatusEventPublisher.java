package com.example.patterns.behavioral.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 订单状态变更事件发布者。
 *
 * <p>观察者模式中的「主题 / 发布者（Subject / Publisher）」角色，负责在订单状态发生变更时
 * 对外发布 {@link OrderStatusChangedEvent}。它仅依赖 Spring 的事件发布抽象
 * {@link ApplicationEventPublisher}，对「有哪些监听者、各自如何处理事件」一无所知，
 * 从而与监听方彻底解耦：新增或移除监听者都无需改动本发布者（满足需求 4.3、10.3，符合开闭原则）。</p>
 *
 * <p>与 Spring 的结合方式：通过 {@link Resource} 注入容器提供的 {@link ApplicationEventPublisher}，
 * 借助其 {@code publishEvent} 完成事件多播；具体的监听注册与分发由 Spring 事件机制负责。</p>
 *
 * @since 1.0.0
 */
@Service
public class OrderStatusEventPublisher {

    /** 日志记录器 */
    private static final Logger log = LoggerFactory.getLogger(OrderStatusEventPublisher.class);

    /** Spring 事件发布器，由容器注入，用于将订单状态变更事件多播给各监听者。 */
    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    /**
     * 发布订单状态变更事件。
     *
     * <p>将传入的事件交由 Spring 事件机制多播给全部已注册的监听者；发布方不感知任何监听者的
     * 存在，仅完成「发布」这一单一职责。事件为空时不予发布，避免向监听者传播无效事件。</p>
     *
     * @param event 待发布的订单状态变更事件；为空时本次发布被忽略
     */
    public void publish(OrderStatusChangedEvent event) {
        if (event == null) {
            log.warn("订单状态变更事件为空，已忽略本次发布。");
            return;
        }
        log.info("发布订单状态变更事件：订单[{}] 状态 {} -> {}",
                event.getOrderNo(), event.getOldStatus(), event.getNewStatus());
        applicationEventPublisher.publishEvent(event);
    }
}
