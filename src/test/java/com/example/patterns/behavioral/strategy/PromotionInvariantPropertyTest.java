package com.example.patterns.behavioral.strategy;

import com.example.patterns.behavioral.strategy.domain.PromotionContext;
import com.example.patterns.behavioral.strategy.domain.PromotionResult;
import com.example.patterns.behavioral.strategy.service.PromotionCalculateService;
import com.example.patterns.behavioral.strategy.strategy.DirectReductionStrategy;
import com.example.patterns.behavioral.strategy.strategy.DiscountStrategy;
import com.example.patterns.behavioral.strategy.strategy.FullReductionStrategy;
import com.example.patterns.behavioral.strategy.strategy.PromotionStrategy;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.GenerationMode;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.DisplayName;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 策略计算不变式属性测试（对应任务 17.4，Property 10，验证需求 4.1）。
 */
class PromotionInvariantPropertyTest {

    private PromotionCalculateService newService() {
        PromotionCalculateService service = new PromotionCalculateService();
        List<PromotionStrategy> strategies = Arrays.asList(
                new FullReductionStrategy(),
                new DiscountStrategy(),
                new DirectReductionStrategy());
        ReflectionTestUtils.setField(service, "promotionStrategies", strategies);
        service.initStrategyMap();
        return service;
    }

    @Provide
    Arbitrary<String> promotionTypes() {
        return Arbitraries.of("full_reduction", "discount", "direct_reduction");
    }

    @Provide
    Arbitrary<BigDecimal> amounts() {
        return Arbitraries.bigDecimals()
                .between(BigDecimal.ZERO, new BigDecimal("100000"))
                .ofScale(2);
    }

    @Provide
    Arbitrary<BigDecimal> discountRates() {
        return Arbitraries.bigDecimals()
                .between(new BigDecimal("0.01"), new BigDecimal("1.00"))
                .ofScale(2);
    }

    // Feature: design-patterns-showcase, Property 10: 策略计算不变式
    @Property(tries = 100, generation = GenerationMode.RANDOMIZED)
    @DisplayName("策略计算恒满足促销不变式")
    void invariantHoldsForAllStrategies(@ForAll("promotionTypes") String type,
                                        @ForAll("amounts") BigDecimal originalAmount,
                                        @ForAll("amounts") BigDecimal threshold,
                                        @ForAll("amounts") BigDecimal reduction,
                                        @ForAll("discountRates") BigDecimal discountRate,
                                        @ForAll("amounts") BigDecimal directAmount) {
        PromotionCalculateService service = newService();
        PromotionContext ctx = new PromotionContext();
        ctx.setOriginalAmount(originalAmount);
        ctx.setThreshold(threshold);
        ctx.setReduction(reduction);
        ctx.setDiscountRate(discountRate);
        ctx.setDirectAmount(directAmount);

        PromotionResult result = service.calculate(type, ctx);

        BigDecimal resultOriginal = result.getOriginalAmount();
        BigDecimal discount = result.getDiscountAmount();
        BigDecimal payable = result.getPayableAmount();

        assertThat(discount).isGreaterThanOrEqualTo(BigDecimal.ZERO);
        assertThat(discount).isLessThanOrEqualTo(resultOriginal);
        assertThat(payable.compareTo(resultOriginal.subtract(discount))).isZero();
    }
}
