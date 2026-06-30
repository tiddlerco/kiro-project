package com.example.patterns.behavioral.strategy.strategy;

import com.example.patterns.behavioral.strategy.domain.PromotionContext;
import com.example.patterns.behavioral.strategy.domain.PromotionResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 折扣促销策略。
 *
 * <p>策略模式中的「具体策略（ConcreteStrategy）」之一：按折扣率对原始金额打折，
 * 优惠额 = 原始金额 ×（1 − 折扣率）。折扣率业务取值区间为 (0,1]（如 0.8 表示打 8 折），
 * 超出该区间的非法取值由 {@link PromotionResult#settle} 结算环节钳制兜底以维持促销不变式。
 * 作为 Spring 组件交由容器管理，由上下文服务按类型标识 {@value #TYPE_DISCOUNT} 路由获取。</p>
 *
 * @since 1.0.0
 */
@Component
public class DiscountStrategy implements PromotionStrategy {

    /**
     * 折扣策略类型标识。
     */
    public static final String TYPE_DISCOUNT = "discount";

    /**
     * 计算折扣优惠结果。
     *
     * <p>先解析出折扣的「原始优惠额」，再交由 {@link PromotionResult#settle} 钳制至合法区间，
     * 从而保证返回结果满足促销不变式。</p>
     *
     * @param ctx 促销计算上下文，使用其中的原始金额与折扣率
     * @return 满足促销不变式的折扣计算结果
     */
    @Override
    public PromotionResult calculate(PromotionContext ctx) {
        BigDecimal rawDiscount = resolveRawDiscount(ctx);
        return PromotionResult.settle(TYPE_DISCOUNT, ctx.getOriginalAmount(), rawDiscount);
    }

    /**
     * 计算折扣的「原始优惠额」（未经区间钳制）。
     *
     * <p>原始金额或折扣率任一缺失时不予优惠（返回 0）；否则按「原始金额 ×（1 − 折扣率）」计算。
     * 折扣率超出 (0,1] 区间时本方法可能返回负值或超过原始金额的值，均由结算环节统一钳制。</p>
     *
     * @param ctx 促销计算上下文
     * @return 折扣的原始优惠额；参数缺失时为 0
     */
    private BigDecimal resolveRawDiscount(PromotionContext ctx) {
        BigDecimal originalAmount = ctx.getOriginalAmount();
        BigDecimal discountRate = ctx.getDiscountRate();
        if (originalAmount == null || discountRate == null) {
            return BigDecimal.ZERO;
        }
        return originalAmount.multiply(BigDecimal.ONE.subtract(discountRate));
    }

    /**
     * 返回折扣策略类型标识。
     *
     * @return 策略类型标识 {@value #TYPE_DISCOUNT}
     */
    @Override
    public String type() {
        return TYPE_DISCOUNT;
    }
}
