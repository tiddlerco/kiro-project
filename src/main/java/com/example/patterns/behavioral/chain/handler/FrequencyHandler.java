package com.example.patterns.behavioral.chain.handler;

import com.example.patterns.behavioral.chain.domain.RiskCheckResult;
import com.example.patterns.behavioral.chain.domain.RiskContext;
import org.springframework.stereotype.Component;

/**
 * 频次风控规则处理器。
 *
 * <p>责任链模式中的「具体处理器（ConcreteHandler）」之一：校验用户近期统计窗口内的交易次数
 * 是否过于频繁，超过阈值则拦截交易，用于防范盗刷、薅羊毛等高频异常行为。排序序号
 * {@link #ORDER} 位于链尾，在身份与金额校验通过后再做频次核验。</p>
 *
 * <p>作为 Spring 组件交由容器管理，由链装配服务以 {@code List} 形式统一注入并按
 * {@link #order()} 组链。本演示以内置的频次阈值常量表示内蕴的频控规则，真实工程中通常由
 * 风控规则配置提供，且交易次数一般来自实时计数或滑动窗口统计。</p>
 *
 * @since 1.0.0
 */
@Component
public class FrequencyHandler implements RiskRuleHandler {

    /**
     * 本节点的排序序号。
     *
     * <p>取较大值使频次校验位于链尾执行。</p>
     */
    public static final int ORDER = 30;

    /**
     * 本规则节点名称，用于在拦截结果中标识命中节点。
     */
    private static final String NODE_NAME = "频次校验";

    /**
     * 统计窗口内允许的最大交易次数（演示用）。
     *
     * <p>交易次数超过该阈值将被拦截；真实工程中应替换为风控规则配置中的频次阈值。</p>
     */
    private static final int MAX_RECENT_TRANSACTION_COUNT = 5;

    /**
     * 校验用户近期交易次数是否超过频次上限。
     *
     * <p>交易次数非空且严格大于频次上限时拦截交易并返回带命中节点与原因的拦截结果；否则放行。
     * 交易次数为空时按不触发频次拦截处理，交由后续节点继续校验。</p>
     *
     * @param ctx 沿链传递的风控校验上下文，使用其中的近期交易次数
     * @return 超过频次上限时为拦截结果，否则为通过结果
     */
    @Override
    public RiskCheckResult handle(RiskContext ctx) {
        Integer recentTransactionCount = ctx.getRecentTransactionCount();
        if (recentTransactionCount != null && recentTransactionCount > MAX_RECENT_TRANSACTION_COUNT) {
            return RiskCheckResult.reject(NODE_NAME,
                    "近期交易次数 " + recentTransactionCount + " 次超过频次上限 " + MAX_RECENT_TRANSACTION_COUNT + " 次");
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
