package com.example.patterns.behavioral.state.state;

import com.example.patterns.behavioral.state.OrderStateContext;

/**
 * 订单状态（State 模式中的「状态」抽象角色）。
 *
 * <p>定义订单在某一具体状态下可被触发的全部动作：支付、发货、完成、取消。每个动作均接收
 * 订单状态上下文 {@link OrderStateContext} 作为参数：当该动作在当前状态下属于合法流转时，
 * 状态对象回调上下文的流转方法完成状态切换与持久化；否则抛出
 * {@link com.example.patterns.common.exception.IllegalStateTransitionException}
 * 拒绝本次流转并保持订单原状态不变（满足需求 4.6）。</p>
 *
 * <p>各具体状态（{@code CreatedState}/{@code PaidState}/{@code ShippedState}/{@code CompletedState}/
 * {@code CancelledState}）均为无内部可变字段的无状态实现，作为 Spring 单例 Bean 被安全共享。</p>
 *
 * @since 1.0.0
 */
public interface OrderState {

    /**
     * 执行「支付」动作。
     *
     * <p>合法时将订单从当前状态流转至已支付（{@code PAID}）；非法时抛出非法状态流转异常。</p>
     *
     * @param context 订单状态上下文，承载当前订单与状态，并提供合法流转的持久化能力
     */
    void pay(OrderStateContext context);

    /**
     * 执行「发货」动作。
     *
     * <p>合法时将订单从当前状态流转至已发货（{@code SHIPPED}）；非法时抛出非法状态流转异常。</p>
     *
     * @param context 订单状态上下文，承载当前订单与状态，并提供合法流转的持久化能力
     */
    void ship(OrderStateContext context);

    /**
     * 执行「完成」动作。
     *
     * <p>合法时将订单从当前状态流转至已完成（{@code COMPLETED}）；非法时抛出非法状态流转异常。</p>
     *
     * @param context 订单状态上下文，承载当前订单与状态，并提供合法流转的持久化能力
     */
    void complete(OrderStateContext context);

    /**
     * 执行「取消」动作。
     *
     * <p>合法时将订单从当前状态流转至已取消（{@code CANCELLED}）；非法时抛出非法状态流转异常。</p>
     *
     * @param context 订单状态上下文，承载当前订单与状态，并提供合法流转的持久化能力
     */
    void cancel(OrderStateContext context);

    /**
     * 返回该状态对应的状态码。
     *
     * <p>状态码用于构建「状态码 → 状态对象」路由表以及向数据库持久化订单状态，
     * 取值见 {@link com.example.patterns.behavioral.state.OrderStatus}。</p>
     *
     * @return 该状态的状态码字符串
     */
    String stateName();
}
