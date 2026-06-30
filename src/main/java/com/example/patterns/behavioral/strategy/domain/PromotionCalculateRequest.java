package com.example.patterns.behavioral.strategy.domain;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 促销计算请求。
 *
 * <p>策略模式演示接口 {@code POST /pattern/strategy/calculate} 的入参对象，承载前端选择的
 * 策略类型标识与一次促销计算所需的各项金额参数。其上的校验注解供控制器以
 * {@code @Validated} 触发声明式参数校验，避免在控制器中手写 if 校验逻辑
 * （与项目统一的请求对象校验规范一致）。</p>
 *
 * <p>本类为纯数据对象（DTO），不承载促销业务逻辑；getter/setter 由 Lombok 的 {@link Data}
 * 注解生成。所有金额与比率均采用 {@link BigDecimal} 表示，避免浮点类型在金额计算中引入
 * 精度误差。除策略类型与原始金额为必填外，其余字段为各策略各自所需的可选参数，未使用的
 * 字段允许为 {@code null}，并通过 {@link #toContext()} 一对一映射为
 * {@link PromotionContext} 后交由服务层计算。</p>
 *
 * @since 1.0.0
 */
@Data
public class PromotionCalculateRequest {

    /**
     * 促销策略类型标识。
     *
     * <p>取值如 {@code "full_reduction"}（满减）、{@code "discount"}（折扣）、
     * {@code "direct_reduction"}（立减），由服务层据此路由到对应策略，必填。</p>
     */
    @NotBlank(message = "促销策略类型不能为空")
    private String type;

    /**
     * 原始金额（单位：元）。
     *
     * <p>促销前的订单应付金额，是各策略计算优惠的基数，必填且业务上要求不小于 0。</p>
     */
    @NotNull(message = "原始金额不能为空")
    private BigDecimal originalAmount;

    /**
     * 满减门槛（单位：元，满减策略使用，可选）。
     *
     * <p>当原始金额达到（大于等于）该门槛时才触发满减优惠。</p>
     */
    private BigDecimal threshold;

    /**
     * 满减额度（单位：元，满减策略使用，可选）。
     *
     * <p>原始金额达到满减门槛后直接减免的固定金额。</p>
     */
    private BigDecimal reduction;

    /**
     * 折扣率（折扣策略使用，可选）。
     *
     * <p>业务取值区间为 (0,1]，例如 {@code 0.8} 表示「打 8 折」。</p>
     */
    private BigDecimal discountRate;

    /**
     * 立减额度（单位：元，立减策略使用，可选）。
     *
     * <p>不论原始金额多少均直接减免的金额。</p>
     */
    private BigDecimal directAmount;

    /**
     * 将当前请求对象映射为促销计算上下文。
     *
     * <p>仅做字段一对一搬运，不承载任何促销业务判断（业务校验与计算由服务层与策略实现负责），
     * 使控制器保持仅路由分发的职责。</p>
     *
     * @return 与本请求字段值一致的促销计算上下文
     */
    public PromotionContext toContext() {
        PromotionContext context = new PromotionContext();
        context.setOriginalAmount(originalAmount);
        context.setThreshold(threshold);
        context.setReduction(reduction);
        context.setDiscountRate(discountRate);
        context.setDirectAmount(directAmount);
        return context;
    }
}
