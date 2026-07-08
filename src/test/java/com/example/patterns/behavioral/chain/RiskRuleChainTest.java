package com.example.patterns.behavioral.chain;

import com.example.patterns.behavioral.chain.domain.RiskCheckResult;
import com.example.patterns.behavioral.chain.domain.RiskContext;
import com.example.patterns.behavioral.chain.service.RiskRuleChain;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 责任链核心行为单元测试。
 */
@SpringBootTest
class RiskRuleChainTest {

    @Resource
    private RiskRuleChain riskRuleChain;

    @Test
    @DisplayName("各规则均不触发时应通过全部节点")
    void shouldPassWhenNoRuleTriggered() {
        RiskContext ctx = buildContext("U_NORMAL_001", new BigDecimal("10000"), 3);

        RiskCheckResult result = riskRuleChain.check(ctx);

        assertTrue(result.isPassed(), "所有规则均不触发时应放行");
        assertNull(result.getHitHandler(), "通过结果不应携带命中节点");
        assertNull(result.getRejectReason(), "通过结果不应携带拦截原因");
    }

    @Test
    @DisplayName("黑名单用户应被黑名单节点拦截")
    void shouldRejectByBlacklistHandler() {
        RiskContext ctx = buildContext("U_BLACK_001", new BigDecimal("100"), 1);

        RiskCheckResult result = riskRuleChain.check(ctx);

        assertFalse(result.isPassed(), "黑名单用户应被拦截");
        assertEquals("黑名单校验", result.getHitHandler(), "命中节点应为黑名单校验");
        assertTrue(result.getRejectReason() != null && result.getRejectReason().contains("黑名单"),
                "拦截原因应说明命中黑名单");
    }

    @Test
    @DisplayName("金额超过单笔上限时应被金额上限节点拦截")
    void shouldRejectByAmountLimitHandler() {
        RiskContext ctx = buildContext("U_NORMAL_002", new BigDecimal("50001"), 1);

        RiskCheckResult result = riskRuleChain.check(ctx);

        assertFalse(result.isPassed(), "金额超过单笔上限时应被拦截");
        assertEquals("金额上限校验", result.getHitHandler(), "命中节点应为金额上限校验");
        assertTrue(result.getRejectReason() != null && result.getRejectReason().contains("上限"),
                "拦截原因应说明超过金额上限");
    }

    @Test
    @DisplayName("频次超过上限时应被频次节点拦截")
    void shouldRejectByFrequencyHandler() {
        RiskContext ctx = buildContext("U_NORMAL_003", new BigDecimal("100"), 6);

        RiskCheckResult result = riskRuleChain.check(ctx);

        assertFalse(result.isPassed(), "频次超过上限时应被拦截");
        assertEquals("频次校验", result.getHitHandler(), "命中节点应为频次校验");
        assertTrue(result.getRejectReason() != null && result.getRejectReason().contains("频次"),
                "拦截原因应说明超过频次上限");
    }

    private RiskContext buildContext(String userId, BigDecimal amount, int recentTransactionCount) {
        RiskContext ctx = new RiskContext();
        ctx.setUserId(userId);
        ctx.setAmount(amount);
        ctx.setRecentTransactionCount(recentTransactionCount);
        return ctx;
    }
}
