package com.example.patterns.behavioral.strategy.strategy;

import com.example.patterns.behavioral.strategy.domain.PromotionContext;
import com.example.patterns.behavioral.strategy.domain.PromotionResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 满减促销策略。
 *
 * <p>策略模式中的「具体策略（ConcreteStrategy）」之一：当原始金额达到（大于等于）满减门槛时，
 * 直接减免配置的固定额度，否则不予优惠（满 {@code threshold} 减 {@code reduction}）。
 * 作为 Spring 组件交由容器管理，由上下文服务按类型标识 {@value #TYPE_FULL_REDUCTION}
 * 路由获取。</p>
 *
 * @since 1.0.0
 */
@Component
public class FullReductionStrategy implements PromotionStrategy {

    /**
     * 满减策略类型标识。
     */
    public static final String TYPE_FULL_REDUCTION = "full_reduction";

    /**
     * 计算满减优惠结果。
     *
     * <p>先解析出满减的「原始优惠额」，再交由 {@link PromotionResult#settle} 钳制至合法区间，
     * 从而保证返回结果满足促销不变式。</p>
     *
     * @param ctx 促销计算上下文，使用其中的原始金额、满减门槛与满减额度
     * @return 满足促销不变式的满减计算结果
     */
    @Override
    public PromotionResult calculate(PromotionContext ctx) {
        BigDecimal rawDiscount = resolveRawDiscount(ctx);
        return PromotionResult.settle(TYPE_FULL_REDUCTION, ctx.getOriginalAmount(), rawDiscount);
    }

    /**
     * 计算满减的「原始优惠额」（未经区间钳制）。
     *
     * <p>仅当原始金额、满减门槛、满减额度均已配置且原始金额达到门槛时，原始优惠额取满减额度；
     * 其余情况一律为 0（不满足门槛或参数缺失均不予优惠）。</p>
     *
     * @param ctx 促销计算上下文
     * @return 达到满减门槛时为满减额度，否则为 0
     */
    private BigDecimal resolveRawDiscount(PromotionContext ctx) {
        BigDecimal originalAmount = ctx.getOriginalAmount();
        BigDecimal threshold = ctx.getThreshold();
        BigDecimal reduction = ctx.getReduction();
        boolean thresholdReached = originalAmount != null && threshold != null && reduction != null
                && originalAmount.compareTo(threshold) >= 0;
        return thresholdReached ? reduction : BigDecimal.ZERO;
    }

    /**
     * 返回满减策略类型标识。
     *
     * @return 策略类型标识 {@value #TYPE_FULL_REDUCTION}
     */
    @Override
    public String type() {
        return TYPE_FULL_REDUCTION;
    }
}
