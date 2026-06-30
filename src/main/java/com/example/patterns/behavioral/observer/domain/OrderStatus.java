package com.example.patterns.behavioral.observer.domain;

/**
 * 订单状态枚举。
 *
 * <p>观察者模式示例中订单状态变更通知所涉及的订单状态取值集合，作为
 * {@link com.example.patterns.behavioral.observer.OrderStatusChangedEvent} 承载的
 * 「原状态」「新状态」字段类型。相较于使用裸字符串表达状态，枚举可在编译期约束取值范围，
 * 并为每个状态附带可读的中文描述，便于各监听者据此生成面向用户的通知文案。</p>
 *
 * <p>本枚举仅作为承载订单状态语义的数据对象，不包含任何状态流转规则；订单状态机的合法
 * 流转约束由「状态模式」示例（{@code behavioral.state}）单独演示，二者关注点不同、互不依赖。</p>
 *
 * @since 1.0.0
 */
public enum OrderStatus {

    /** 已创建：订单已生成、等待买家付款。 */
    CREATED("已创建"),

    /** 已支付：买家已完成付款、等待商家发货。 */
    PAID("已支付"),

    /** 已发货：商家已发货、等待买家确认收货。 */
    SHIPPED("已发货"),

    /** 已完成：买家已确认收货、交易正常结束。 */
    COMPLETED("已完成"),

    /** 已取消：订单被取消、交易终止。 */
    CANCELLED("已取消");

    /** 状态的中文描述，用于生成面向用户的可读通知文案。 */
    private final String description;

    /**
     * 构造订单状态枚举项。
     *
     * @param description 状态的中文描述
     */
    OrderStatus(String description) {
        this.description = description;
    }

    /**
     * 获取状态的中文描述。
     *
     * @return 状态的中文描述，如「已支付」
     */
    public String getDescription() {
        return description;
    }
}
