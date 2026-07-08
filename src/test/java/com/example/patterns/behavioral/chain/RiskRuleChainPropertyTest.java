package com.example.patterns.behavioral.chain;

import com.example.patterns.behavioral.chain.domain.RiskCheckResult;
import com.example.patterns.behavioral.chain.domain.RiskContext;
import com.example.patterns.behavioral.chain.handler.AmountLimitHandler;
import com.example.patterns.behavioral.chain.handler.BlacklistHandler;
import com.example.patterns.behavioral.chain.handler.FrequencyHandler;
import com.example.patterns.behavioral.chain.handler.RiskRuleHandler;
import com.example.patterns.behavioral.chain.service.RiskRuleChain;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.GenerationMode;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.DisplayName;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 责任链传递与短路属性测试。
 */
class RiskRuleChainPropertyTest {

    // Feature: design-patterns-showcase, Property 11: 责任链传递与短路
    @Property(tries = 100, generation = GenerationMode.RANDOMIZED)
    @DisplayName("责任链结果通过当且仅当全部节点放行且拦截后短路")
    void shouldPassIffAllHandlersAllowAndShortCircuitAfterReject(@ForAll("风控上下文") RiskContext ctx) {
        List<String> executionTrace = new ArrayList<>();

        List<RiskRuleHandler> realHandlers = Arrays.asList(
                new BlacklistHandler(), new AmountLimitHandler(), new FrequencyHandler());

        List<RiskRuleHandler> shuffledProbes = new ArrayList<>();
        for (int i = realHandlers.size() - 1; i >= 0; i--) {
            RiskRuleHandler real = realHandlers.get(i);
            shuffledProbes.add(new RecordingRiskRuleHandler(real, real.getClass().getSimpleName(), executionTrace));
        }

        RiskRuleChain chain = assembleChain(shuffledProbes);
        RiskCheckResult actualResult = chain.check(ctx);

        List<String> expectedTrace = new ArrayList<>();
        boolean expectedPassed = true;
        String expectedHitHandler = null;
        for (RiskRuleHandler real : realHandlers) {
            expectedTrace.add(real.getClass().getSimpleName());
            RiskCheckResult singleResult = real.handle(ctx);
            if (!singleResult.isPassed()) {
                expectedPassed = false;
                expectedHitHandler = singleResult.getHitHandler();
                break;
            }
        }

        assertEquals(expectedPassed, actualResult.isPassed(),
                "链的通过与否应当且仅当全部节点均放行；上下文=" + ctx);

        assertEquals(expectedTrace, executionTrace,
                "被拦截后其后节点不应再被执行（短路）；上下文=" + ctx);

        if (!expectedPassed) {
            assertEquals(expectedHitHandler, actualResult.getHitHandler(),
                    "命中节点应为链上首个拦截节点；上下文=" + ctx);
        }
    }

    @Provide("风控上下文")
    Arbitrary<RiskContext> riskContexts() {
        Arbitrary<String> userIds = Arbitraries
                .of("U_BLACK_001", "U_BLACK_002", "U_NORMAL_A", "U_NORMAL_B")
                .injectNull(0.1);
        Arbitrary<BigDecimal> amounts = Arbitraries.longs().between(0L, 100000L).map(BigDecimal::valueOf);
        Arbitrary<Integer> recentCounts = Arbitraries.integers().between(0, 10);
        return Combinators.combine(userIds, amounts, recentCounts).as((uid, amt, cnt) -> {
            RiskContext ctx = new RiskContext();
            ctx.setUserId(uid);
            ctx.setAmount(amt);
            ctx.setRecentTransactionCount(cnt);
            return ctx;
        });
    }

    private RiskRuleChain assembleChain(List<RiskRuleHandler> handlers) {
        try {
            RiskRuleChain chain = new RiskRuleChain();
            Field field = RiskRuleChain.class.getDeclaredField("riskRuleHandlers");
            field.setAccessible(true);
            field.set(chain, handlers);
            chain.initChain();
            return chain;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("装配责任链失败", e);
        }
    }
}
