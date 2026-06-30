package com.example.patterns.behavioral.chain.domain;

import lombok.Data;

/**
 * 风控校验结果。
 *
 * <p>承载责任链一次风控校验的可观察结果：是否通过全部规则、若被拦截则命中的规则节点名称
 * 及其拦截原因。既作为单个 {@link com.example.patterns.behavioral.chain.handler.RiskRuleHandler#handle}
 * 节点的返回值，也作为整条链 {@code RiskRuleChain} 驱动后的最终返回值，供调用方据此放行
 * 或拒绝交易并展示拦截详情。</p>
 *
 * <p>本类为纯数据对象（DTO），getter/setter 由 Lombok 的 {@link Data} 注解生成；构造合法
 * 结果的唯一入口为 {@link #pass()} 与 {@link #reject(String, String)} 两个静态工厂方法：
 * 「通过」结果不含命中节点与拦截原因，「拦截」结果必带命中节点与拦截原因，避免出现
 * 「通过却携带拦截原因」之类语义自相矛盾的中间态。</p>
 *
 * @since 1.0.0
 */
@Data
public class RiskCheckResult {

    /**
     * 是否通过校验。
     *
     * <p>{@code true} 表示放行（单个节点放行交由后续节点，或整条链全部通过）；
     * {@code false} 表示被拦截。</p>
     */
    private boolean passed;

    /**
     * 命中（拦截）的规则节点名称。
     *
     * <p>仅在被拦截（{@link #passed} 为 {@code false}）时有值，用于定位是哪一个风控规则
     * 拦截了本次交易；通过时为 {@code null}。</p>
     */
    private String hitHandler;

    /**
     * 拦截原因。
     *
     * <p>仅在被拦截时有值，用于向调用方说明交易被拒绝的具体原因；通过时为 {@code null}。</p>
     */
    private String rejectReason;

    /**
     * 构造一个「通过」的风控校验结果。
     *
     * <p>表示放行：作为节点返回值时表示本节点放行、请求继续沿链传递；作为整条链返回值时
     * 表示请求通过了全部规则节点。该结果不含命中节点与拦截原因。</p>
     *
     * @return 表示通过的风控校验结果
     */
    public static RiskCheckResult pass() {
        RiskCheckResult result = new RiskCheckResult();
        result.setPassed(true);
        return result;
    }

    /**
     * 构造一个「被拦截」的风控校验结果。
     *
     * <p>表示请求在某规则节点处被拦截：一旦产生该结果，链将立即短路、其后的规则节点不再执行。</p>
     *
     * @param hitHandler   命中（拦截）本次交易的规则节点名称
     * @param rejectReason 拦截原因，用于向调用方说明交易被拒绝的具体原因
     * @return 表示被指定节点拦截的风控校验结果
     */
    public static RiskCheckResult reject(String hitHandler, String rejectReason) {
        RiskCheckResult result = new RiskCheckResult();
        result.setPassed(false);
        result.setHitHandler(hitHandler);
        result.setRejectReason(rejectReason);
        return result;
    }
}
