package com.example.patterns.behavioral.strategy.domain;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 促销计算上下文。
 *
 * <p>承载一次促销优惠计算所需的全部输入：原始金额以及各类策略各自所需的参数。
 * 作为 {@link com.example.patterns.behavioral.strategy.strategy.PromotionStrategy#calculate}
 * 的统一入参，使满减、折扣、立减等不同策略在不改变调用方代码的前提下相互替换
 * （策略模式的核心诉求，对应需求 4.1）。</p>
 *
 * <p>本类为纯数据对象（DTO），不承载任何促销业务逻辑；getter/setter 由 Lombok
 * 的 {@link Data} 注解生成。所有金额与比率均采用 {@link BigDecimal} 表示，
 * 避免浮点类型在金额计算中引入精度误差（金融场景的基本要求）。各策略仅读取与
 * 自身相关的字段，未使用的字段允许为 {@code null}。</p>
 *
 * @since 1.0.0
 */
@Data
public class PromotionContext {

    /**
     * 原始金额（单位：元）。
     *
     * <p>促销前的订单应付金额，是各策略计算优惠的基数，业务上要求非空且不小于 0。</p>
     */
    private BigDecimal originalAmount;

    /**
     * 满减门槛（单位：元，满减策略使用）。
     *
     * <p>当原始金额达到（大于等于）该门槛时才触发满减优惠，否则不予优惠。</p>
     */
    private BigDecimal threshold;

    /**
     * 满减额度（单位：元，满减策略使用）。
     *
     * <p>原始金额达到满减门槛后直接减免的固定金额。</p>
     */
    private BigDecimal reduction;

    /**
     * 折扣率（折扣策略使用）。
     *
     * <p>业务取值区间为 (0,1]，例如 {@code 0.8} 表示「打 8 折」，对应优惠额为
     * 原始金额的 20%。超出该区间的非法取值由结算环节钳制兜底，以维持促销不变式。</p>
     */
    private BigDecimal discountRate;

    /**
     * 立减额度（单位：元，立减策略使用）。
     *
     * <p>不论原始金额多少均直接减免的金额；超过原始金额时由结算环节钳制为不超过原始金额。</p>
     */
    private BigDecimal directAmount;
}
