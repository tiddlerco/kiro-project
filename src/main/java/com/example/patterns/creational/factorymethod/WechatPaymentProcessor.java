package com.example.patterns.creational.factorymethod;

import com.example.patterns.creational.factorymethod.domain.PayResult;
import com.example.patterns.creational.factorymethod.domain.PaymentContext;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 微信支付处理器。
 *
 * <p>工厂方法模式中的「具体产品（ConcreteProduct）」角色之一，实现微信渠道的支付逻辑。
 * 作为 Spring 组件交由容器管理，由 {@link PaymentProcessorFactory} 按渠道标识
 * {@value #CHANNEL_WECHAT} 路由获取。</p>
 *
 * @since 1.0.0
 */
@Component
public class WechatPaymentProcessor implements PaymentProcessor {

    /**
     * 微信支付渠道标识。
     */
    private static final String CHANNEL_WECHAT = "wechat";

    /**
     * 微信支付流水号前缀。
     */
    private static final String TRANSACTION_PREFIX = "WX";

    /**
     * 执行微信支付。
     *
     * <p>生成微信渠道的支付流水号，并返回填充完成的成功支付结果。</p>
     *
     * @param ctx 支付请求上下文，承载订单号、金额、付款用户等支付要素
     * @return 表示微信支付成功的支付结果
     */
    @Override
    public PayResult pay(PaymentContext ctx) {
        String transactionId = generateTransactionId();
        return PayResult.success(CHANNEL_WECHAT, ctx.getOrderNo(), transactionId, ctx.getAmount(), "微信支付成功");
    }

    /**
     * 返回微信支付渠道标识。
     *
     * @return 渠道标识 {@value #CHANNEL_WECHAT}
     */
    @Override
    public String channel() {
        return CHANNEL_WECHAT;
    }

    /**
     * 生成微信支付流水号。
     *
     * <p>以固定前缀拼接去除分隔符并转为大写的 UUID，模拟渠道侧返回的唯一交易凭证编号。</p>
     *
     * @return 以 {@value #TRANSACTION_PREFIX} 开头的全局唯一支付流水号
     */
    private String generateTransactionId() {
        return TRANSACTION_PREFIX + UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }
}
