package com.example.patterns.behavioral.state.state;

import com.example.patterns.behavioral.state.OrderStateContext;
import com.example.patterns.behavioral.state.OrderStatus;
import com.example.patterns.common.exception.IllegalStateTransitionException;
import org.springframework.stereotype.Component;

/**
 * 已支付状态（ConcreteState）。
 *
 * <p>订单完成支付后的状态。合法流转：发货 → 已发货（{@code SHIPPED}）、取消 → 已取消（{@code CANCELLED}）；
 * 支付与完成动作在该状态下非法，将被拒绝并保持原状态不变。</p>
 *
 * @since 1.0.0
 */
@Component
public class PaidState implements OrderState {

    /**
     * 在已支付状态下执行支付：非法动作（不可重复支付），拒绝流转并保持原状态不变。
     *
     * @param context 订单状态上下文
     */
    @Override
    public void pay(OrderStateContext context) {
        throw new IllegalStateTransitionException("订单处于【已支付】状态，不允许重复执行【支付】动作（非法状态流转）");
    }

    /**
     * 在已支付状态下执行发货：合法流转至已发货状态。
     *
     * @param context 订单状态上下文
     */
    @Override
    public void ship(OrderStateContext context) {
        context.transitionTo(OrderStatus.SHIPPED);
    }

    /**
     * 在已支付状态下执行完成：非法动作（需先发货），拒绝流转并保持原状态不变。
     *
     * @param context 订单状态上下文
     */
    @Override
    public void complete(OrderStateContext context) {
        throw new IllegalStateTransitionException("订单处于【已支付】状态，不允许执行【完成】动作（非法状态流转）");
    }

    /**
     * 在已支付状态下执行取消：合法流转至已取消状态。
     *
     * @param context 订单状态上下文
     */
    @Override
    public void cancel(OrderStateContext context) {
        context.transitionTo(OrderStatus.CANCELLED);
    }

    /**
     * 返回该状态对应的状态码。
     *
     * @return 状态码 {@link OrderStatus#PAID}
     */
    @Override
    public String stateName() {
        return OrderStatus.PAID;
    }
}
