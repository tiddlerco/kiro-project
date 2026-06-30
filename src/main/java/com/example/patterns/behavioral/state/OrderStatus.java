package com.example.patterns.behavioral.state;

/**
 * 订单状态码常量。
 *
 * <p>集中定义订单状态机的全部合法状态码，与数据库 {@code biz_order.status} 字段取值一致。
 * 状态对象（{@code OrderState} 实现）以这些常量自报状态码，状态上下文据此构建「状态码 → 状态对象」
 * 路由表并持久化新状态，从而消除散落各处的魔法字符串、保证状态码的一致性（落实 C10 表达力优先）。</p>
 *
 * <p>合法流转关系（详见 design.md「17. 状态 State」）：</p>
 * <ul>
 *     <li>{@link #CREATED} → {@link #PAID} / {@link #CANCELLED}</li>
 *     <li>{@link #PAID} → {@link #SHIPPED} / {@link #CANCELLED}</li>
 *     <li>{@link #SHIPPED} → {@link #COMPLETED}</li>
 *     <li>{@link #COMPLETED}、{@link #CANCELLED} 为终态，不再允许任何流转</li>
 * </ul>
 *
 * @since 1.0.0
 */
public final class OrderStatus {

    /**
     * 已创建：订单初始状态。
     */
    public static final String CREATED = "CREATED";

    /**
     * 已支付：订单完成支付后的状态。
     */
    public static final String PAID = "PAID";

    /**
     * 已发货：订单完成发货后的状态。
     */
    public static final String SHIPPED = "SHIPPED";

    /**
     * 已完成：订单流转的正常终态。
     */
    public static final String COMPLETED = "COMPLETED";

    /**
     * 已取消：订单流转的取消终态。
     */
    public static final String CANCELLED = "CANCELLED";

    /**
     * 私有构造方法。
     *
     * <p>本类为纯常量类，不应被实例化，故将构造方法私有化以禁止外部 {@code new}。</p>
     */
    private OrderStatus() {
    }
}
