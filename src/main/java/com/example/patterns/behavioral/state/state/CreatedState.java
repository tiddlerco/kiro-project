package com.example.patterns.behavioral.state.state;

import com.example.patterns.behavioral.state.OrderStateContext;
import com.example.patterns.behavioral.state.OrderStatus;
import com.example.patterns.common.exception.IllegalStateTransitionException;
import org.springframework.stereotype.Component;

/**
 * 已创建状态（ConcreteState）。
 *
 * <p>订单的初始状态。合法流转：支付 → 已支付（{@code PAID}）、取消 → 已取消（{@code CANCELLED}）；
 * 发货与完成动作在该状态下非法，将被拒绝并保持原状态不变。</p>
 *
 * @since 1.0.0
 */
@Component
public class CreatedState implements OrderState {

    /**
     * 在已创建状态下执行支付：合法流转至已支付状态。
     *
     * @param context 订单状态上下文
     */
    @Override
    public void pay(OrderStateContext context) {
        context.transitionTo(OrderStatus.PAID);
    }

    /**
     * 在已创建状态下执行发货：非法动作，拒绝流转并保持原状态不变。
     *
     * @param context 订单状态上下文
     */
    @Override
    public void ship(OrderStateContext context) {
        throw new IllegalStateTransitionException("订单处于【已创建】状态，不允许执行【发货】动作（非法状态流转）");
    }

    /**
     * 在已创建状态下执行完成：非法动作，拒绝流转并保持原状态不变。
     *
     * @param context 订单状态上下文
     */
    @Override
    public void complete(OrderStateContext context) {
        throw new IllegalStateTransitionException("订单处于【已创建】状态，不允许执行【完成】动作（非法状态流转）");
    }

    /**
     * 在已创建状态下执行取消：合法流转至已取消状态。
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
     * @return 状态码 {@link OrderStatus#CREATED}
     */
    @Override
    public String stateName() {
        return OrderStatus.CREATED;
    }
}
