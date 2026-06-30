package com.example.patterns.behavioral.observer.controller;

import com.example.patterns.behavioral.observer.OrderStatusChangedEvent;
import com.example.patterns.behavioral.observer.OrderStatusEventPublisher;
import com.example.patterns.behavioral.observer.domain.OrderStatusChangeRequest;
import com.example.patterns.common.core.controller.BaseController;
import com.example.patterns.common.core.domain.AjaxResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 观察者模式演示控制器。
 *
 * <p>对外暴露订单状态变更的 HTTP 演示入口，演示观察者模式「一次状态变更事件被短信通知、
 * 积分发放等多个相互独立的监听者各自响应」的运行效果。控制器仅负责路由分发：接收并校验请求、
 * 构造订单状态变更事件并交由 {@link OrderStatusEventPublisher} 发布，不感知有哪些监听者、
 * 各自如何响应，与监听方彻底解耦。</p>
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("/pattern/observer")
public class OrderStatusController extends BaseController {

    /**
     * 订单状态变更事件发布者（观察者模式的主题 / 发布者角色）。
     */
    @Resource
    private OrderStatusEventPublisher orderStatusEventPublisher;

    /**
     * 变更订单状态并发布状态变更事件。
     *
     * <p>由 {@code @Validated} 触发请求对象的声明式校验，将请求映射为不可变的订单状态变更
     * 事件后委派发布者对外发布；事件经 Spring 事件机制多播给短信通知、积分发放等多个监听者，
     * 由各监听者独立响应（响应结果以各自日志体现）。本方法仅做路由分发，不承载任何业务逻辑。</p>
     *
     * @param request 订单状态变更请求，含订单号、变更前状态、变更后状态与操作人
     * @return 统一成功响应，表示事件已发布并触发各监听者响应
     */
    @PostMapping("/changeOrderStatus")
    public AjaxResult changeOrderStatus(@Validated @RequestBody OrderStatusChangeRequest request) {
        OrderStatusChangedEvent event = OrderStatusChangedEvent.of(
                request.getOrderNo(), request.getOldStatus(), request.getNewStatus(), request.getOperator());
        orderStatusEventPublisher.publish(event);
        return success();
    }
}
