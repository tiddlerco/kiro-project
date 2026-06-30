package com.example.patterns.creational.factorymethod;

import com.example.patterns.creational.factorymethod.domain.PayResult;
import com.example.patterns.creational.factorymethod.domain.PaymentContext;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 支付宝支付处理器。
 *
 * <p>工厂方法模式中的「具体产品（ConcreteProduct）」角色之一，实现支付宝渠道的支付逻辑。
 * 作为 Spring 组件交由容器管理，由 {@link PaymentProcessorFactory} 按渠道标识
 * {@value #CHANNEL_ALIPAY} 路由获取。</p>
 *
 * @since 1.0.0
 */
@Component
public class AlipayPaymentProcessor implements PaymentProcessor {

    /**
     * 支付宝支付渠道标识。
     */
    private static final String CHANNEL_ALIPAY = "alipay";

    /**
     * 支付宝支付流水号前缀。
     */
    private static final String TRANSACTION_PREFIX = "ALI";

    /**
     * 执行支付宝支付。
     *
     * <p>生成支付宝渠道的支付流水号，并返回填充完成的成功支付结果。</p>
     *
     * @param ctx 支付请求上下文，承载订单号、金额、付款用户等支付要素
     * @return 表示支付宝支付成功的支付结果
     */
    @Override
    public PayResult pay(PaymentContext ctx) {
        String transactionId = generateTransactionId();
        return PayResult.success(CHANNEL_ALIPAY, ctx.getOrderNo(), transactionId, ctx.getAmount(), "支付宝支付成功");
    }

    /**
     * 返回支付宝支付渠道标识。
     *
     * @return 渠道标识 {@value #CHANNEL_ALIPAY}
     */
    @Override
    public String channel() {
        return CHANNEL_ALIPAY;
    }

    /**
     * 生成支付宝支付流水号。
     *
     * <p>以固定前缀拼接去除分隔符并转为大写的 UUID，模拟渠道侧返回的唯一交易凭证编号。</p>
     *
     * @return 以 {@value #TRANSACTION_PREFIX} 开头的全局唯一支付流水号
     */
    private String generateTransactionId() {
        return TRANSACTION_PREFIX + UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }
}
