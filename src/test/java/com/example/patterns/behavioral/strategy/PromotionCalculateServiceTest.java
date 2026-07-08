package com.example.patterns.behavioral.strategy;

import com.example.patterns.behavioral.strategy.domain.PromotionContext;
import com.example.patterns.behavioral.strategy.domain.PromotionResult;
import com.example.patterns.behavioral.strategy.service.PromotionCalculateService;
import com.example.patterns.behavioral.strategy.strategy.DirectReductionStrategy;
import com.example.patterns.behavioral.strategy.strategy.DiscountStrategy;
import com.example.patterns.behavioral.strategy.strategy.FullReductionStrategy;
import com.example.patterns.behavioral.strategy.strategy.PromotionStrategy;
import com.example.patterns.common.exception.ServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 策略模式核心行为单元测试（对应任务 17.3，验证需求 4.1）。
 *
 * <p>验证 {@link PromotionCalculateService} 依据类型标识路由到满减、折扣、立减三种具体策略后
 * 计算出的促销结果符合预期：满减达标减免固定额、未达标不优惠；折扣按折扣率计算优惠额；
 * 立减按立减额减免（不超过原始金额）；未知类型标识抛出 {@link ServiceException}。</p>
 *
 * <p>采用「手动装配」方式构造被测服务：直接 new 出三种具体策略，以 {@link ReflectionTestUtils}
 * 注入服务的策略列表字段后，主动触发 {@code initStrategyMap()} 构建「类型标识 → 策略」路由表，
 * 无需启动 Spring 上下文，简洁可靠。金额一律以 {@link BigDecimal#compareTo} 断言，规避标度差异。</p>
 *
 * @since 1.0.0
 */
class PromotionCalculateServiceTest {

    /**
     * 构造一个已完成策略装配与路由表初始化的促销计算服务。
     *
     * <p>不启动 Spring 上下文，直接 new 满减/折扣/立减三种具体策略并反射注入策略列表字段，
     * 再显式调用 {@code initStrategyMap()} 复刻 {@code @PostConstruct} 行为，
     * 使「类型标识 → 策略」路由表就绪，作为各用例的统一被测对象来源。</p>
     *
     * @return 已完成依赖装配且路由表就绪、可直接计算的促销计算服务
     */
    private PromotionCalculateService newService() {
        PromotionCalculateService service = new PromotionCalculateService();
        java.util.List<PromotionStrategy> strategies = Arrays.asList(
                new FullReductionStrategy(),
                new DiscountStrategy(),
                new DirectReductionStrategy());
        ReflectionTestUtils.setField(service, "promotionStrategies", strategies);
        service.initStrategyMap();
        return service;
    }

    /**
     * 构造一个促销计算上下文。
     *
     * @param originalAmount 原始金额
     * @param threshold      满减门槛（可为空）
     * @param reduction      满减额度（可为空）
     * @param discountRate   折扣率（可为空）
     * @param directAmount   立减额度（可为空）
     * @return 装配完成的促销计算上下文
     */
    private PromotionContext buildContext(BigDecimal originalAmount, BigDecimal threshold,
                                          BigDecimal reduction, BigDecimal discountRate,
                                          BigDecimal directAmount) {
        PromotionContext ctx = new PromotionContext();
        ctx.setOriginalAmount(originalAmount);
        ctx.setThreshold(threshold);
        ctx.setReduction(reduction);
        ctx.setDiscountRate(discountRate);
        ctx.setDirectAmount(directAmount);
        return ctx;
    }

    /**
     * 场景①：满减达标——原始金额达到门槛时，优惠额等于满减额度、优惠后金额等于原始金额减满减额度。
     */
    @Test
    @DisplayName("满减达标_优惠额为满减额度且优惠后金额正确")
    void calculate_fullReductionThresholdReached_shouldApplyReduction() {
        PromotionCalculateService service = newService();
        // 原始 200 元，满 100 减 30，已达门槛
        PromotionContext ctx = buildContext(new BigDecimal("200.00"), new BigDecimal("100.00"),
                new BigDecimal("30.00"), null, null);

        PromotionResult result = service.calculate("full_reduction", ctx);

        assertNotNull(result);
        assertEquals("full_reduction", result.getAppliedType());
        // 优惠额 = 满减额度 30
        assertEquals(0, new BigDecimal("30.00").compareTo(result.getDiscountAmount()), "优惠额应为满减额度");
        // 优惠后金额 = 原始 200 - 优惠 30 = 170
        assertEquals(0, new BigDecimal("170.00").compareTo(result.getPayableAmount()), "优惠后金额应为原始金额减满减额度");
    }

    /**
     * 场景②：满减未达标——原始金额未达门槛时，优惠额为 0、优惠后金额等于原始金额。
     */
    @Test
    @DisplayName("满减未达标_不优惠且优惠后金额等于原始金额")
    void calculate_fullReductionThresholdNotReached_shouldNotApplyReduction() {
        PromotionCalculateService service = newService();
        // 原始 50 元，满 100 减 30，未达门槛
        PromotionContext ctx = buildContext(new BigDecimal("50.00"), new BigDecimal("100.00"),
                new BigDecimal("30.00"), null, null);

        PromotionResult result = service.calculate("full_reduction", ctx);

        // 未达门槛优惠额为 0
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getDiscountAmount()), "未达门槛优惠额应为 0");
        // 优惠后金额等于原始金额
        assertEquals(0, new BigDecimal("50.00").compareTo(result.getPayableAmount()), "未达门槛优惠后金额应等于原始金额");
    }

    /**
     * 场景③：折扣——按折扣率 0.8（8 折）计算，优惠额 = 原始金额 ×（1 − 0.8），优惠后金额正确。
     */
    @Test
    @DisplayName("折扣_按折扣率计算优惠额与优惠后金额")
    void calculate_discount_shouldApplyDiscountRate() {
        PromotionCalculateService service = newService();
        // 原始 100 元，打 8 折（discountRate=0.8），优惠额 = 100 × 0.2 = 20
        PromotionContext ctx = buildContext(new BigDecimal("100.00"), null, null,
                new BigDecimal("0.8"), null);

        PromotionResult result = service.calculate("discount", ctx);

        assertEquals("discount", result.getAppliedType());
        // 优惠额 = 原始 × (1 - 0.8) = 20
        assertEquals(0, new BigDecimal("20.00").compareTo(result.getDiscountAmount()), "优惠额应为原始金额乘以(1-折扣率)");
        // 优惠后金额 = 100 - 20 = 80
        assertEquals(0, new BigDecimal("80.00").compareTo(result.getPayableAmount()), "优惠后金额应为原始金额减优惠额");
    }

    /**
     * 场景④：立减——按立减额度直接减免（不超过原始金额），优惠额等于立减额度、优惠后金额正确。
     */
    @Test
    @DisplayName("立减_优惠额为立减额度且优惠后金额正确")
    void calculate_directReduction_shouldApplyDirectAmount() {
        PromotionCalculateService service = newService();
        // 原始 100 元，立减 15 元（不超过原始金额）
        PromotionContext ctx = buildContext(new BigDecimal("100.00"), null, null,
                null, new BigDecimal("15.00"));

        PromotionResult result = service.calculate("direct_reduction", ctx);

        assertEquals("direct_reduction", result.getAppliedType());
        // 优惠额 = 立减额度 15
        assertEquals(0, new BigDecimal("15.00").compareTo(result.getDiscountAmount()), "优惠额应为立减额度");
        // 优惠后金额 = 100 - 15 = 85
        assertEquals(0, new BigDecimal("85.00").compareTo(result.getPayableAmount()), "优惠后金额应为原始金额减立减额度");
    }

    /**
     * 场景⑤：未知策略类型标识应抛出 {@link ServiceException}（非静默失败）。
     */
    @Test
    @DisplayName("未知策略类型_抛出ServiceException")
    void calculate_unknownType_shouldThrowServiceException() {
        PromotionCalculateService service = newService();
        PromotionContext ctx = buildContext(new BigDecimal("100.00"), null, null, null, null);

        // 未知类型标识应被路由查找拒绝并抛出业务异常
        assertThrows(ServiceException.class, () -> service.calculate("unknown_type", ctx));
    }
}
