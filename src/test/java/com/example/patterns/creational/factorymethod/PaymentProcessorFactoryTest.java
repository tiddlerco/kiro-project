package com.example.patterns.creational.factorymethod;

import com.example.patterns.creational.factorymethod.domain.PayResult;
import com.example.patterns.creational.factorymethod.domain.PaymentContext;
import com.example.patterns.common.exception.ServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 工厂方法模式核心行为单元测试（对应任务 4.3，验证需求 11.4、2.4）。
 *
 * <p>验证 {@link PaymentProcessorFactory} 依据渠道标识路由到对应的具体支付处理器：
 * 受支持渠道（{@code wechat}/{@code alipay}）返回其自身声明渠道标识与请求一致的处理器，
 * 且经该处理器执行支付得到成功的 {@link PayResult}；未知渠道则抛出 {@link ServiceException}
 * 而不创建任何处理器实例（工厂仅做路由查找，处理器为预先装配的单例）。</p>
 *
 * <p>采用「手动装配」方式构造被测工厂：直接 new 出微信、支付宝两种具体处理器，以
 * {@link ReflectionTestUtils} 注入工厂的处理器列表字段后，主动调用 {@code initProcessorMap()}
 * 复刻 {@code @PostConstruct} 行为构建「渠道标识 → 处理器」路由表，无需启动 Spring 上下文，
 * 简洁可靠。金额一律以 {@link BigDecimal#compareTo} 断言，规避标度差异。</p>
 *
 * @since 1.0.0
 */
class PaymentProcessorFactoryTest {

    /**
     * 微信支付渠道标识。
     */
    private static final String CHANNEL_WECHAT = "wechat";

    /**
     * 支付宝支付渠道标识。
     */
    private static final String CHANNEL_ALIPAY = "alipay";

    /**
     * 构造一个已完成处理器装配与路由表初始化的支付处理器工厂。
     *
     * <p>不启动 Spring 上下文，直接 new 微信、支付宝两种具体处理器并反射注入处理器列表字段，
     * 再显式调用 {@code initProcessorMap()} 复刻 {@code @PostConstruct} 行为，
     * 使「渠道标识 → 处理器」路由表就绪，作为各用例的统一被测对象来源。</p>
     *
     * @return 已完成依赖装配且路由表就绪、可直接创建处理器的支付处理器工厂
     */
    private PaymentProcessorFactory newFactory() {
        PaymentProcessorFactory factory = new PaymentProcessorFactory();
        List<PaymentProcessor> processors = Arrays.asList(
                new WechatPaymentProcessor(),
                new AlipayPaymentProcessor());
        ReflectionTestUtils.setField(factory, "paymentProcessors", processors);
        factory.initProcessorMap();
        return factory;
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
     * 场景①：受支持渠道 {@code wechat}——工厂返回渠道标识为 {@code wechat} 的处理器，
     * 且经其执行支付得到成功、渠道与金额均正确的支付结果。
     */
    @Test
    @DisplayName("受支持渠道wechat_返回微信处理器且支付成功")
    void create_wechatChannel_shouldReturnWechatProcessorAndPaySuccess() {
        PaymentProcessorFactory factory = newFactory();

        PaymentProcessor processor = factory.create(CHANNEL_WECHAT);

        assertNotNull(processor);
        assertEquals(CHANNEL_WECHAT, processor.channel(), "返回处理器自身声明的渠道标识应与请求一致");

        PaymentContext ctx = buildContext("ORDER-WX-001", new BigDecimal("88.88"));
        PayResult result = processor.pay(ctx);

        assertNotNull(result);
        assertTrue(result.isSuccess(), "微信支付应成功");
        assertEquals(CHANNEL_WECHAT, result.getChannel(), "支付结果渠道应为 wechat");
        assertEquals("ORDER-WX-001", result.getOrderNo(), "支付结果订单号应无损透传");
        assertEquals(0, new BigDecimal("88.88").compareTo(result.getAmount()), "支付结果金额应与上下文一致");
        assertNotNull(result.getTransactionId(), "应生成支付流水号");
        assertNotNull(result.getPayTime(), "应填充支付完成时间");
    }

    /**
     * 场景②：受支持渠道 {@code alipay}——工厂返回渠道标识为 {@code alipay} 的处理器，
     * 且经其执行支付得到成功、渠道与金额均正确的支付结果。
     */
    @Test
    @DisplayName("受支持渠道alipay_返回支付宝处理器且支付成功")
    void create_alipayChannel_shouldReturnAlipayProcessorAndPaySuccess() {
        PaymentProcessorFactory factory = newFactory();

        PaymentProcessor processor = factory.create(CHANNEL_ALIPAY);

        assertNotNull(processor);
        assertEquals(CHANNEL_ALIPAY, processor.channel(), "返回处理器自身声明的渠道标识应与请求一致");

        PaymentContext ctx = buildContext("ORDER-ALI-001", new BigDecimal("199.00"));
        PayResult result = processor.pay(ctx);

        assertNotNull(result);
        assertTrue(result.isSuccess(), "支付宝支付应成功");
        assertEquals(CHANNEL_ALIPAY, result.getChannel(), "支付结果渠道应为 alipay");
        assertEquals("ORDER-ALI-001", result.getOrderNo(), "支付结果订单号应无损透传");
        assertEquals(0, new BigDecimal("199.00").compareTo(result.getAmount()), "支付结果金额应与上下文一致");
        assertNotNull(result.getTransactionId(), "应生成支付流水号");
        assertNotNull(result.getPayTime(), "应填充支付完成时间");
    }

    /**
     * 场景③：工厂为纯路由查找而非每次创建实例——对同一受支持渠道多次调用 {@code create}
     * 应恒返回同一预先装配的处理器实例（引用相等），佐证工厂不在调用时新建处理器实例。
     */
    @Test
    @DisplayName("同一渠道多次获取_返回同一处理器实例不重复创建")
    void create_sameChannelMultipleTimes_shouldReturnSameInstance() {
        PaymentProcessorFactory factory = newFactory();

        PaymentProcessor first = factory.create(CHANNEL_WECHAT);
        PaymentProcessor second = factory.create(CHANNEL_WECHAT);

        assertSame(first, second, "同一渠道多次获取应返回同一实例，工厂仅路由查找不重复创建");
    }

    /**
     * 场景④：未知渠道边界——{@code create("unknown")} 抛出 {@link ServiceException}，
     * 且路由表规模保持不变（未新增任何处理器条目），佐证未知渠道不触发实例创建或副作用。
     */
    @Test
    @DisplayName("未知渠道_抛出ServiceException且不创建任何处理器实例")
    @SuppressWarnings("unchecked")
    void create_unknownChannel_shouldThrowServiceExceptionAndCreateNoInstance() {
        PaymentProcessorFactory factory = newFactory();

        Map<String, PaymentProcessor> processorMap =
                (Map<String, PaymentProcessor>) ReflectionTestUtils.getField(factory, "processorMap");
        assertNotNull(processorMap);
        int sizeBefore = processorMap.size();

        // 未知渠道应被路由查找拒绝并抛出业务异常（非静默失败）
        assertThrows(ServiceException.class, () -> factory.create("unknown"));

        // 抛出异常后路由表规模不变，未创建/登记任何新处理器实例
        assertEquals(sizeBefore, processorMap.size(), "未知渠道不应创建或登记任何处理器实例");
    }

    /**
     * 场景⑤：空白渠道边界——{@code create("")} 抛出 {@link ServiceException}，
     * 覆盖渠道标识为空的非法输入路径。
     */
    @Test
    @DisplayName("空白渠道标识_抛出ServiceException")
    void create_blankChannel_shouldThrowServiceException() {
        PaymentProcessorFactory factory = newFactory();

        assertThrows(ServiceException.class, () -> factory.create(""));
    }
}
