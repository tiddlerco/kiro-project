package com.example.patterns.behavioral.chain.handler;

import com.example.patterns.behavioral.chain.domain.RiskCheckResult;
import com.example.patterns.behavioral.chain.domain.RiskContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 黑名单风控规则处理器。
 *
 * <p>责任链模式中的「具体处理器（ConcreteHandler）」之一：校验交易发起用户是否处于风控黑名单，
 * 命中黑名单则拦截交易。作为风控链中风险最高、代价最低的硬性规则，置于链首（{@link #ORDER}
 * 最小）优先执行，以便在投入金额、频次等后续计算前尽早短路拦截。</p>
 *
 * <p>作为 Spring 组件交由容器管理，由链装配服务以 {@code List} 形式统一注入并按
 * {@link #order()} 组链。本演示以内置的黑名单用户集合表示内蕴的风控名单，真实工程中通常由
 * 风控名单库或缓存提供。</p>
 *
 * @since 1.0.0
 */
@Component
public class BlacklistHandler implements RiskRuleHandler {

    /**
     * 本节点的排序序号。
     *
     * <p>取最小值使黑名单校验位于链首优先执行。</p>
     */
    public static final int ORDER = 10;

    /**
     * 本规则节点名称，用于在拦截结果中标识命中节点。
     */
    private static final String NODE_NAME = "黑名单校验";

    /**
     * 风控黑名单用户集合（内蕴名单，演示用）。
     *
     * <p>以不可变集合表示，避免运行期被意外篡改；真实工程中应替换为名单库或缓存查询。</p>
     */
    private static final Set<String> BLACKLIST_USER_IDS =
            Collections.unmodifiableSet(new HashSet<>(Arrays.asList("U_BLACK_001", "U_BLACK_002")));

    /**
     * 校验用户是否处于风控黑名单。
     *
     * <p>用户标识非空且命中黑名单时拦截交易并返回带命中节点与原因的拦截结果；否则放行。
     * 用户标识为空时按非黑名单处理，交由后续节点继续校验。</p>
     *
     * @param ctx 沿链传递的风控校验上下文，使用其中的用户标识
     * @return 命中黑名单时为拦截结果，否则为通过结果
     */
    @Override
    public RiskCheckResult handle(RiskContext ctx) {
        String userId = ctx.getUserId();
        if (userId != null && BLACKLIST_USER_IDS.contains(userId)) {
            return RiskCheckResult.reject(NODE_NAME, "用户【" + userId + "】处于风控黑名单，禁止交易");
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
