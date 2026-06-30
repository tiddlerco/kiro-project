package com.example.patterns.behavioral.chain.handler;

import com.example.patterns.behavioral.chain.domain.RiskCheckResult;
import com.example.patterns.behavioral.chain.domain.RiskContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 金额上限风控规则处理器。
 *
 * <p>责任链模式中的「具体处理器（ConcreteHandler）」之一：校验本次交易金额是否超过单笔交易
 * 限额，超限则拦截交易。排序序号 {@link #ORDER} 位于黑名单之后、频次之前，体现「先核身份、
 * 再核金额、后核频次」的风控编排顺序。</p>
 *
 * <p>作为 Spring 组件交由容器管理，由链装配服务以 {@code List} 形式统一注入并按
 * {@link #order()} 组链。本演示以内置的单笔限额常量表示内蕴的限额规则，真实工程中通常由
 * 风控规则配置提供。金额比较采用 {@link BigDecimal#compareTo} 以规避浮点精度问题。</p>
 *
 * @since 1.0.0
 */
@Component
public class AmountLimitHandler implements RiskRuleHandler {

    /**
     * 本节点的排序序号。
     *
     * <p>位于黑名单（10）之后、频次（30）之前。</p>
     */
    public static final int ORDER = 20;

    /**
     * 本规则节点名称，用于在拦截结果中标识命中节点。
     */
    private static final String NODE_NAME = "金额上限校验";

    /**
     * 单笔交易金额上限（单位：元，演示用）。
     *
     * <p>超过该上限的交易将被拦截；真实工程中应替换为风控规则配置中的限额值。</p>
     */
    private static final BigDecimal SINGLE_AMOUNT_LIMIT = new BigDecimal("50000");

    /**
     * 校验本次交易金额是否超过单笔限额。
     *
     * <p>金额非空且严格大于单笔限额时拦截交易并返回带命中节点与原因的拦截结果；否则放行。
     * 金额为空时按不触发金额上限处理，交由后续节点继续校验。</p>
     *
     * @param ctx 沿链传递的风控校验上下文，使用其中的交易金额
     * @return 超过单笔限额时为拦截结果，否则为通过结果
     */
    @Override
    public RiskCheckResult handle(RiskContext ctx) {
        BigDecimal amount = ctx.getAmount();
        if (amount != null && amount.compareTo(SINGLE_AMOUNT_LIMIT) > 0) {
            return RiskCheckResult.reject(NODE_NAME,
                    "单笔交易金额 " + amount + " 元超过上限 " + SINGLE_AMOUNT_LIMIT + " 元");
        }
        return RiskCheckResult.pass();
    }

    /**
     * 返回本节点的排序序号。
     *
     * @return 排序序号 {@value #ORDER}
     */
    @Override
    public int order() {
        return ORDER;
    }
}
