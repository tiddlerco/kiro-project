package com.example.patterns.behavioral.state.state;

import com.example.patterns.behavioral.state.OrderStateContext;
import com.example.patterns.behavioral.state.OrderStatus;
import com.example.patterns.common.exception.IllegalStateTransitionException;
import org.springframework.stereotype.Component;

/**
 * 已发货状态（ConcreteState）。
 *
 * <p>订单完成发货后的状态。合法流转：完成 → 已完成（{@code COMPLETED}）；
 * 支付、发货、取消动作在该状态下非法，将被拒绝并保持原状态不变。</p>
 *
 * @since 1.0.0
 */
@Component
public class ShippedState implements OrderState {

    /**
     * 在已发货状态下执行支付：非法动作（已发货不可再支付），拒绝流转并保持原状态不变。
     *
     * @param context 订单状态上下文
     */
    @Override
    public void pay(OrderStateContext context) {
        throw new IllegalStateTransitionException("订单处于【已发货】状态，不允许执行【支付】动作（非法状态流转）");
    }

    /**
     * 在已发货状态下执行发货：非法动作（不可重复发货），拒绝流转并保持原状态不变。
     *
     * @param context 订单状态上下文
     */
    @Override
    public void ship(OrderStateContext context) {
        throw new IllegalStateTransitionException("订单处于【已发货】状态，不允许重复执行【发货】动作（非法状态流转）");
    }

    /**
     * 在已发货状态下执行完成：合法流转至已完成状态。
     *
     * @param context 订单状态上下文
     */
    @Override
    public void complete(OrderStateContext context) {
        context.transitionTo(OrderStatus.COMPLETED);
    }

    /**
     * 在已发货状态下执行取消：非法动作（已发货不可取消），拒绝流转并保持原状态不变。
     *
     * @param context 订单状态上下文
     */
    @Override
    public void cancel(OrderStateContext context) {
        throw new IllegalStateTransitionException("订单处于【已发货】状态，不允许执行【取消】动作（非法状态流转）");
    }

    /**
     * 返回该状态对应的状态码。
     *
     * @return 状态码 {@link OrderStatus#SHIPPED}
     */
    @Override
    public String stateName() {
        return OrderStatus.SHIPPED;
    }
}
