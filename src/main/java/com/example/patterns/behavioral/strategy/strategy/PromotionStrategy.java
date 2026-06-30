package com.example.patterns.behavioral.strategy.strategy;

import com.example.patterns.behavioral.strategy.domain.PromotionContext;
import com.example.patterns.behavioral.strategy.domain.PromotionResult;

/**
 * 促销策略。
 *
 * <p>策略模式中的「抽象策略（Strategy）」角色，抽象出「依据促销上下文计算优惠结果」这一
 * 可替换的算法族。满减、折扣、立减等不同优惠规则以独立实现类承担「具体策略
 * （ConcreteStrategy）」角色，由上下文服务
 * {@link com.example.patterns.behavioral.strategy.service.PromotionCalculateService}
 * 依据类型标识选取并委派计算，调用方仅依赖本接口而无需感知具体策略实现（依赖倒置）。</p>
 *
 * <p>所有实现均须保证返回结果满足促销不变式（优惠额 ≥ 0、优惠额 ≤ 原始金额、
 * 优惠后金额 = 原始金额 − 优惠额），该不变式由
 * {@link PromotionResult#settle} 统一钳制保证。</p>
 *
 * @since 1.0.0
 */
public interface PromotionStrategy {

    /**
     * 依据促销上下文计算优惠结果。
     *
     * @param ctx 促销计算上下文，承载原始金额与本策略所需的参数
     * @return 满足促销不变式的促销计算结果
     */
    PromotionResult calculate(PromotionContext ctx);

    /**
     * 返回当前策略所属的类型标识。
     *
     * <p>该标识由实现类自身声明，供上下文服务建立「类型标识 → 策略」路由表，
     * 因而须在所有实现之间保持唯一，不依赖 Spring 的 bean 名称。</p>
     *
     * @return 策略类型标识，如 {@code "full_reduction"}、{@code "discount"}、{@code "direct_reduction"}
     */
    String type();
}
