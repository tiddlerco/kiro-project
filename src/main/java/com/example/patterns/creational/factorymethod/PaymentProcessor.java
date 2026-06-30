package com.example.patterns.creational.factorymethod;

import com.example.patterns.creational.factorymethod.domain.PayResult;
import com.example.patterns.creational.factorymethod.domain.PaymentContext;

/**
 * 支付处理器。
 *
 * <p>工厂方法模式中的「抽象产品（Product）」角色，抽象出各支付渠道的统一支付能力。
 * 不同支付渠道（微信、支付宝等）以独立实现类承担「具体产品（ConcreteProduct）」角色，
 * 由 {@link PaymentProcessorFactory} 依据渠道标识创建并返回对应实现，
 * 调用方仅依赖本接口而无需感知具体渠道实现（依赖倒置）。</p>
 *
 * @since 1.0.0
 */
public interface PaymentProcessor {

    /**
     * 执行支付。
     *
     * @param ctx 支付请求上下文，承载订单号、金额、付款用户等支付要素
     * @return 支付结果，包含渠道、流水号、金额与完成时间等可观察信息
     */
    PayResult pay(PaymentContext ctx);

    /**
     * 返回当前处理器所属的支付渠道标识。
     *
     * <p>该标识由实现类自身声明，供工厂建立「渠道标识 → 处理器」路由表，
     * 因而须在所有实现之间保持唯一，不依赖 Spring 的 bean 名称。</p>
     *
     * @return 渠道标识，如 {@code "wechat"}、{@code "alipay"}
     */
    String channel();
}
