package com.example.patterns.behavioral.state.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体（状态模式 State 的持久化载体）。
 *
 * <p>对应数据库表 {@code biz_order}，承载订单状态机流转所需的核心字段。订单的当前状态
 * 以 {@link #status} 字段表示，取值范围为 {@code CREATED/PAID/SHIPPED/COMPLETED/CANCELLED}
 * （集中定义于 {@link com.example.patterns.behavioral.state.OrderStatus}）。状态机每次合法流转后，
 * 由 {@code OrderStateContext} 通过 {@code OrderMapper.updateStatus} 将新状态持久化回该表。</p>
 *
 * <p>列名与属性名的映射依赖全局开启的下划线转驼峰（如 {@code order_no -> orderNo}），
 * 同时在 {@code OrderMapper.xml} 中以显式 {@code resultMap} 双重保障字段映射准确。</p>
 *
 * @since 1.0.0
 */
@Data
public class OrderEntity {

    /**
     * 主键 id。
     */
    private Long id;

    /**
     * 订单号（业务唯一）。
     */
    private String orderNo;

    /**
     * 订单金额。
     */
    private BigDecimal amount;

    /**
     * 订单状态：CREATED（已创建）/PAID（已支付）/SHIPPED（已发货）/COMPLETED（已完成）/CANCELLED（已取消）。
     */
    private String status;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;

    /**
     * 更新时间（状态流转时刷新）。
     */
    private LocalDateTime updateTime;
}
