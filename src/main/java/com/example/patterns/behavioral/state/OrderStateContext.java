package com.example.patterns.behavioral.state;

import com.example.patterns.behavioral.state.entity.OrderEntity;
import com.example.patterns.behavioral.state.mapper.OrderMapper;
import com.example.patterns.behavioral.state.state.OrderState;
import com.example.patterns.common.constant.HttpStatus;
import com.example.patterns.common.exception.IllegalStateTransitionException;
import com.example.patterns.common.exception.ServiceException;

import java.util.Map;
import java.util.Objects;

/**
 * 订单状态上下文（State 模式中的「上下文 Context」角色）。
 *
 * <p>持有当前订单 {@link OrderEntity} 与其对应的当前状态对象 {@link OrderState}，对外提供
 * 支付、发货、完成、取消四个动作入口，并将每个动作委派给当前状态对象处理，从而把「不同状态下
 * 同一动作的差异行为」分散到各具体状态类中（State 模式核心）。</p>
 *
 * <p>流转与持久化：当某动作在当前状态下属于合法流转时，状态对象回调本上下文的
 * {@link #transitionTo(String)} 完成「先持久化、后切换内存状态」；当动作非法时，状态对象抛出
 * {@link IllegalStateTransitionException}，本上下文不会发生任何状态变更，订单保持原状态不变
 * （满足需求 4.6）。</p>
 *
 * <p>协作方式：本上下文是「每个订单一个实例」的有状态对象，故设计为普通 POJO（非 Spring Bean），
 * 由上层服务在每次流转时按订单创建。其依赖的「状态码 → 状态对象」路由表 {@code stateRegistry}
 * 可由上层服务基于容器注入的 {@code List<OrderState>}（各具体状态 Bean）一次性构建后复用，
 * {@link OrderMapper} 用于将合法流转后的新状态写回 {@code biz_order}。</p>
 *
 * @since 1.0.0
 */
public class OrderStateContext {

    /**
     * 当前订单。
     *
     * <p>其 {@code status} 字段始终与 {@link #currentState} 保持一致，合法流转后被同步刷新。</p>
     */
    private final OrderEntity order;

    /**
     * 「状态码 → 状态对象」路由表（只读复用）。
     *
     * <p>键为状态码（见 {@link OrderStatus}），值为对应的无状态状态对象，用于按状态码解析状态实例。</p>
     */
    private final Map<String, OrderState> stateRegistry;

    /**
     * 订单持久化 Mapper，用于在合法流转后写回新状态。
     */
    private final OrderMapper orderMapper;

    /**
     * 当前状态对象，初始值依据订单当前状态码解析得到。
     */
    private OrderState currentState;

    /**
     * 构造订单状态上下文。
     *
     * <p>构造时校验各协作对象非空，并依据订单当前的状态码解析出初始状态对象；若订单状态码不在
     * 已知状态集合内（数据异常），抛出 {@link ServiceException}。</p>
     *
     * @param order         当前订单，要求非空且 {@code id}、{@code status} 均不为空
     * @param stateRegistry 「状态码 → 状态对象」路由表，要求非空且包含全部合法状态
     * @param orderMapper   订单持久化 Mapper，要求非空
     */
    public OrderStateContext(OrderEntity order, Map<String, OrderState> stateRegistry, OrderMapper orderMapper) {
        this.order = Objects.requireNonNull(order, "订单不能为空");
        this.stateRegistry = Objects.requireNonNull(stateRegistry, "状态路由表不能为空");
        this.orderMapper = Objects.requireNonNull(orderMapper, "订单 Mapper 不能为空");
        Objects.requireNonNull(order.getId(), "订单 id 不能为空");
        this.currentState = resolveState(order.getStatus());
    }

    /**
     * 触发「支付」动作。
     *
     * <p>委派当前状态对象处理：合法则流转至已支付，非法则抛出非法状态流转异常且状态不变。</p>
     */
    public void pay() {
        currentState.pay(this);
    }

    /**
     * 触发「发货」动作。
     *
     * <p>委派当前状态对象处理：合法则流转至已发货，非法则抛出非法状态流转异常且状态不变。</p>
     */
    public void ship() {
        currentState.ship(this);
    }

    /**
     * 触发「完成」动作。
     *
     * <p>委派当前状态对象处理：合法则流转至已完成，非法则抛出非法状态流转异常且状态不变。</p>
     */
    public void complete() {
        currentState.complete(this);
    }

    /**
     * 触发「取消」动作。
     *
     * <p>委派当前状态对象处理：合法则流转至已取消，非法则抛出非法状态流转异常且状态不变。</p>
     */
    public void cancel() {
        currentState.cancel(this);
    }

    /**
     * 执行一次合法状态流转：先持久化新状态，成功后再切换内存中的当前状态。
     *
     * <p>仅供具体状态对象在确认动作合法后回调。先调用 {@link OrderMapper#updateStatus(Long, String)}
     * 将新状态写回数据库；当受影响行数不为 1（如订单不存在）时抛出 {@link ServiceException} 且不切换内存状态，
     * 保证内存与数据库一致。持久化成功后再同步刷新订单状态字段与当前状态对象。</p>
     *
     * @param targetStateName 目标状态码，取值见 {@link OrderStatus}
     */
    public void transitionTo(String targetStateName) {
        OrderState targetState = resolveState(targetStateName);
        int affected = orderMapper.updateStatus(order.getId(), targetStateName);
        if (affected != 1) {
            throw new ServiceException(HttpStatus.ERROR,
                    "订单状态持久化失败，订单可能不存在，订单 id：" + order.getId());
        }
        order.setStatus(targetStateName);
        this.currentState = targetState;
    }

    /**
     * 依据状态码从路由表解析对应的状态对象。
     *
     * <p>状态码为空或不在已知状态集合内时抛出 {@link ServiceException}，避免静默返回 {@code null}
     * 导致后续空指针。</p>
     *
     * @param statusCode 状态码，取值见 {@link OrderStatus}
     * @return 与状态码匹配的状态对象
     */
    private OrderState resolveState(String statusCode) {
        if (statusCode == null || statusCode.trim().isEmpty()) {
            throw new ServiceException(HttpStatus.ERROR, "订单状态码不能为空");
        }
        OrderState state = stateRegistry.get(statusCode);
        if (state == null) {
            throw new ServiceException(HttpStatus.ERROR, "未知的订单状态码：" + statusCode);
        }
        return state;
    }

    /**
     * 获取当前订单。
     *
     * @return 当前订单实体，其状态字段反映最近一次合法流转后的最新状态
     */
    public OrderEntity getOrder() {
        return order;
    }

    /**
     * 获取当前状态对象。
     *
     * @return 当前状态对象
     */
    public OrderState getCurrentState() {
        return currentState;
    }

    /**
     * 获取当前状态码。
     *
     * @return 当前状态对象对应的状态码
     */
    public String getCurrentStateName() {
        return currentState.stateName();
    }
}
