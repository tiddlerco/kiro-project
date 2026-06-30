package com.example.patterns.behavioral.strategy.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 促销计算结果。
 *
 * <p>承载某个促销策略计算后的可观察结果：实际应用的策略类型、原始金额、优惠额与
 * 优惠后应付金额。作为 {@link com.example.patterns.behavioral.strategy.strategy.PromotionStrategy#calculate}
 * 的返回值，供调用方据此展示并核对优惠计算结果。</p>
 *
 * <p>本类为纯数据对象（DTO），getter/setter 由 Lombok 的 {@link Data} 注解生成；
 * 同时提供 {@link #settle} 静态工厂方法，作为构造合法结果的唯一入口，在其中统一
 * 钳制优惠额并装配各字段，确保返回结果恒满足以下「促销不变式」（为后续 Property 10 铺垫）：</p>
 * <ul>
 *     <li>优惠额 ≥ 0；</li>
 *     <li>优惠额 ≤ 原始金额；</li>
 *     <li>优惠后金额 = 原始金额 − 优惠额。</li>
 * </ul>
 *
 * <p>金额一律采用 {@link BigDecimal} 表示以保证精度，优惠额统一规整到「分」（两位小数）。</p>
 *
 * @since 1.0.0
 */
@Data
public class PromotionResult {

    /**
     * 金额计算保留的小数位数（2 位，即精确到「分」）。
     */
    private static final int AMOUNT_SCALE = 2;

    /**
     * 实际应用的促销策略类型标识。
     *
     * <p>取自实际执行计算的策略所声明的类型标识，如 {@code "full_reduction"}、{@code "discount"}。</p>
     */
    private String appliedType;

    /**
     * 原始金额（单位：元）。
     */
    private BigDecimal originalAmount;

    /**
     * 优惠额（单位：元）。
     *
     * <p>恒满足 0 ≤ 优惠额 ≤ 原始金额，由 {@link #settle} 钳制保证。</p>
     */
    private BigDecimal discountAmount;

    /**
     * 优惠后应付金额（单位：元）。
     *
     * <p>恒等于「原始金额 − 优惠额」，由 {@link #settle} 计算保证。</p>
     */
    private BigDecimal payableAmount;

    /**
     * 依据原始金额与「原始优惠额」结算出满足促销不变式的计算结果。
     *
     * <p>各具体策略只需计算出未经约束的「原始优惠额」并交由本方法结算，由本方法统一完成
     * 「规整到分 → 下界钳制为 0 → 上界钳制为原始金额 → 计算优惠后金额」，从而保证无论
     * 策略与参数如何，返回结果均满足促销不变式。</p>
     *
     * @param appliedType       实际应用的策略类型标识
     * @param originalAmount    原始金额；为空或为负时按 0 处理以保证不变式可成立
     * @param rawDiscountAmount 策略计算出的原始优惠额（未经区间钳制）；为空时按 0 处理
     * @return 各字段填充完成且满足促销不变式的促销计算结果
     */
    public static PromotionResult settle(String appliedType, BigDecimal originalAmount, BigDecimal rawDiscountAmount) {
        BigDecimal original = normalizeOriginalAmount(originalAmount);
        BigDecimal discount = clampDiscount(original, rawDiscountAmount);
        BigDecimal payable = original.subtract(discount);
        PromotionResult result = new PromotionResult();
        result.setAppliedType(appliedType);
        result.setOriginalAmount(original);
        result.setDiscountAmount(discount);
        result.setPayableAmount(payable);
        return result;
    }

    /**
     * 规整原始金额：空值或负值统一按 0 处理。
     *
     * <p>这是保证促销不变式（优惠额 ≥ 0 且 ≤ 原始金额）能够成立的前提；业务入口
     * （上下文服务）已对负金额显式拒绝，此处为结算环节的兜底防御。</p>
     *
     * @param originalAmount 原始金额，可能为空或为负
     * @return 规整后的非负原始金额
     */
    private static BigDecimal normalizeOriginalAmount(BigDecimal originalAmount) {
        if (originalAmount == null || originalAmount.signum() < 0) {
            return BigDecimal.ZERO;
        }
        return originalAmount;
    }

    /**
     * 将「原始优惠额」规整到分并钳制到 [0, 原始金额] 区间。
     *
     * <p>先按四舍五入规整到两位小数，再以 0 为下界、原始金额为上界进行钳制，
     * 使得优惠额恒满足 0 ≤ 优惠额 ≤ 原始金额。</p>
     *
     * @param original          已规整为非负的原始金额，作为优惠额的上界
     * @param rawDiscountAmount 策略计算出的原始优惠额（未经区间钳制），可能为空
     * @return 落在 [0, 原始金额] 区间内且精确到分的优惠额
     */
    private static BigDecimal clampDiscount(BigDecimal original, BigDecimal rawDiscountAmount) {
        BigDecimal raw = rawDiscountAmount == null ? BigDecimal.ZERO : rawDiscountAmount;
        BigDecimal scaled = raw.setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
        return scaled.max(BigDecimal.ZERO).min(original);
    }
}
