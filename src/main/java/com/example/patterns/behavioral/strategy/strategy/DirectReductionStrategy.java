package com.example.patterns.behavioral.strategy.strategy;

import com.example.patterns.behavioral.strategy.domain.PromotionContext;
import com.example.patterns.behavioral.strategy.domain.PromotionResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 立减促销策略。
 *
 * <p>策略模式中的「具体策略（ConcreteStrategy）」之一：不论原始金额多少，均按配置的立减额度
 * 直接减免；立减额度超过原始金额时，由 {@link PromotionResult#settle} 结算环节钳制为不超过
 * 原始金额。作为 Spring 组件交由容器管理，由上下文服务按类型标识 {@value #TYPE_DIRECT_REDUCTION}
 * 路由获取。</p>
 *
 * @since 1.0.0
 */
@Component
public class DirectReductionStrategy implements PromotionStrategy {

    /**
     * 立减策略类型标识。
     */
    public static final String TYPE_DIRECT_REDUCTION = "direct_reduction";

    /**
     * 计算立减优惠结果。
     *
     * <p>以配置的立减额度作为「原始优惠额」（缺失时按 0 处理），再交由
     * {@link PromotionResult#settle} 钳制至合法区间，从而保证返回结果满足促销不变式。</p>
     *
     * @param ctx 促销计算上下文，使用其中的原始金额与立减额度
     * @return 满足促销不变式的立减计算结果
     */
    @Override
    public PromotionResult calculate(PromotionContext ctx) {
        BigDecimal directAmount = ctx.getDirectAmount();
        BigDecimal rawDiscount = directAmount == null ? BigDecimal.ZERO : directAmount;
        return PromotionResult.settle(TYPE_DIRECT_REDUCTION, ctx.getOriginalAmount(), rawDiscount);
    }

    /**
     * 返回立减策略类型标识。
     *
     * @return 策略类型标识 {@value #TYPE_DIRECT_REDUCTION}
     */
    @Override
    public String type() {
        return TYPE_DIRECT_REDUCTION;
    }
}
