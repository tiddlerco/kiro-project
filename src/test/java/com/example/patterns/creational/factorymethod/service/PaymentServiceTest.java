package com.example.patterns.creational.factorymethod.service;

import com.example.patterns.creational.factorymethod.AlipayPaymentProcessor;
import com.example.patterns.creational.factorymethod.PaymentProcessor;
import com.example.patterns.creational.factorymethod.PaymentProcessorFactory;
import com.example.patterns.creational.factorymethod.WechatPaymentProcessor;
import com.example.patterns.creational.factorymethod.domain.PayResult;
import com.example.patterns.creational.factorymethod.domain.PaymentContext;
import com.example.patterns.common.exception.ServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 支付服务核心行为单元测试（对应任务 4.3，验证需求 11.4、2.4）。
 *
 * <p>验证 {@link PaymentService#pay(String, PaymentContext)} 完整业务动作：委派
 * {@link PaymentProcessorFactory} 依渠道标识取得对应处理器并执行支付。受支持渠道
 * （{@code wechat}/{@code alipay}）返回渠道正确的成功 {@link PayResult}；未知渠道由工厂
 * 抛出 {@link ServiceException}，服务不做拦截、不静默失败。</p>
 *
 * <p>采用「手动装配」方式构造被测对象：直接 new 出微信、支付宝两种具体处理器注入工厂并
 * 触发其 {@code initProcessorMap()} 初始化路由表，再将工厂反射注入服务，无需启动 Spring
 * 上下文，简洁可靠。金额一律以 {@link BigDecimal#compareTo} 断言，规避标度差异。</p>
 *
 * @since 1.0.0
 */
class PaymentServiceTest {

    /**
     * 微信支付渠道标识。
     */
    private static final String CHANNEL_WECHAT = "wechat";

    /**
     * 支付宝支付渠道标识。
     */
    private static final String CHANNEL_ALIPAY = "alipay";

    /**
     * 构造一个已完成工厂装配与路由表初始化的支付服务。
     *
     * <p>不启动 Spring 上下文：先手动装配含微信、支付宝两种处理器的工厂并初始化其路由表，
     * 再以 {@link ReflectionTestUtils} 将工厂注入支付服务的工厂字段，作为各用例的统一被测对象来源。</p>
     *
     * @return 已完成依赖装配、可直接执行支付的支付服务
     */
    private PaymentService newService() {
        PaymentProcessorFactory factory = new PaymentProcessorFactory();
        List<PaymentProcessor> processors = Arrays.asList(
                new WechatPaymentProcessor(),
                new AlipayPaymentProcessor());
        ReflectionTestUtils.setField(factory, "paymentProcessors", processors);
        factory.initProcessorMap();

        PaymentService service = new PaymentService();
        ReflectionTestUtils.setField(service, "paymentProcessorFactory", factory);
        return service;
    }

    /**
     * 构造一笔支付请求上下文。
     *
     * @param orderNo 订单号
     * @param amount  支付金额（单位：元）
     * @return 装配完成的支付请求上下文
     */
    private PaymentContext buildContext(String orderNo, BigDecimal amount) {
        PaymentContext ctx = new PaymentContext();
        ctx.setOrderNo(orderNo);
        ctx.setAmount(amount);
        ctx.setUserId("user-001");
        ctx.setSubject("测试商品");
        return ctx;
    }

    /**
     * 场景①：经服务发起微信支付——返回渠道为 {@code wechat} 的成功支付结果，金额与订单号无损透传。
     */
    @Test
    @DisplayName("服务发起wechat支付_返回微信渠道成功结果")
    void pay_wechatChannel_shouldReturnWechatSuccessResult() {
        PaymentService service = newService();
        PaymentContext ctx = buildContext("ORDER-WX-100", new BigDecimal("66.60"));

        PayResult result = service.pay(CHANNEL_WECHAT, ctx);

        assertNotNull(result);
        assertTrue(result.isSuccess(), "微信支付应成功");
        assertEquals(CHANNEL_WECHAT, result.getChannel(), "支付结果渠道应为 wechat");
        assertEquals("ORDER-WX-100", result.getOrderNo(), "订单号应无损透传");
        assertEquals(0, new BigDecimal("66.60").compareTo(result.getAmount()), "金额应与上下文一致");
    }

    /**
     * 场景②：经服务发起支付宝支付——返回渠道为 {@code alipay} 的成功支付结果，金额与订单号无损透传。
     */
    @Test
    @DisplayName("服务发起alipay支付_返回支付宝渠道成功结果")
    void pay_alipayChannel_shouldReturnAlipaySuccessResult() {
        PaymentService service = newService();
        PaymentContext ctx = buildContext("ORDER-ALI-100", new BigDecimal("128.00"));

        PayResult result = service.pay(CHANNEL_ALIPAY, ctx);

        assertNotNull(result);
        assertTrue(result.isSuccess(), "支付宝支付应成功");
        assertEquals(CHANNEL_ALIPAY, result.getChannel(), "支付结果渠道应为 alipay");
        assertEquals("ORDER-ALI-100", result.getOrderNo(), "订单号应无损透传");
        assertEquals(0, new BigDecimal("128.00").compareTo(result.getAmount()), "金额应与上下文一致");
    }

    /**
     * 场景③：未知渠道边界——{@code pay("unknown", ctx)} 由工厂抛出 {@link ServiceException}，
     * 服务不拦截、不静默失败。
     */
    @Test
    @DisplayName("服务发起未知渠道支付_抛出ServiceException")
    void pay_unknownChannel_shouldThrowServiceException() {
        PaymentService service = newService();
        PaymentContext ctx = buildContext("ORDER-UNKNOWN-001", new BigDecimal("10.00"));

        assertThrows(ServiceException.class, () -> service.pay("unknown", ctx));
    }
}
