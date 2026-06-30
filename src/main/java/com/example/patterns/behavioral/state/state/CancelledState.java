package com.example.patterns.behavioral.state.state;

import com.example.patterns.behavioral.state.OrderStateContext;
import com.example.patterns.behavioral.state.OrderStatus;
import com.example.patterns.common.exception.IllegalStateTransitionException;
import org.springframework.stereotype.Component;

/**
 * 已取消状态（ConcreteState）。
 *
 * <p>订单流转的取消终态。该状态为终态，支付、发货、完成、取消等任何动作均非法，
 * 将被拒绝并保持原状态不变。</p>
 *
 * @since 1.0.0
 */
@Component
public class CancelledState implements OrderState {

    /**
     * 在已取消状态下执行支付：非法动作（终态不可再流转），拒绝流转并保持原状态不变。
     *
     * @param context 订单状态上下文
     */
    @Override
    public void pay(OrderStateContext context) {
        throw new IllegalStateTransitionException("订单处于【已取消】状态（终态），不允许执行【支付】动作（非法状态流转）");
    }

    /**
     * 在已取消状态下执行发货：非法动作（终态不可再流转），拒绝流转并保持原状态不变。
     *
     * @param context 订单状态上下文
     */
    @Override
    public void ship(OrderStateContext context) {
        throw new IllegalStateTransitionException("订单处于【已取消】状态（终态），不允许执行【发货】动作（非法状态流转）");
    }

    /**
     * 在已取消状态下执行完成：非法动作（终态不可流转至完成），拒绝流转并保持原状态不变。
     *
     * @param context 订单状态上下文
     */
    @Override
    public void complete(OrderStateContext context) {
        throw new IllegalStateTransitionException("订单处于【已取消】状态（终态），不允许执行【完成】动作（非法状态流转）");
    }

    /**
     * 在已取消状态下执行取消：非法动作（终态不可重复取消），拒绝流转并保持原状态不变。
     *
     * @param context 订单状态上下文
     */
    @Override
    public void cancel(OrderStateContext context) {
        throw new IllegalStateTransitionException("订单处于【已取消】状态（终态），不允许重复执行【取消】动作（非法状态流转）");
    }

    /**
     * 返回该状态对应的状态码。
     *
     * @return 状态码 {@link OrderStatus#CANCELLED}
     */
    @Override
    public String stateName() {
        return OrderStatus.CANCELLED;
    }
}
