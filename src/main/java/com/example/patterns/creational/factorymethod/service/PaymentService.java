package com.example.patterns.creational.factorymethod.service;

import com.example.patterns.creational.factorymethod.PaymentProcessor;
import com.example.patterns.creational.factorymethod.PaymentProcessorFactory;
import com.example.patterns.creational.factorymethod.domain.PayResult;
import com.example.patterns.creational.factorymethod.domain.PaymentContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 支付服务。
 *
 * <p>工厂方法模式演示的业务服务层，封装「按支付渠道选取处理器并执行支付」这一完整业务动作：
 * 委派 {@link PaymentProcessorFactory} 依据渠道标识取得对应的 {@link PaymentProcessor}，
 * 再由该处理器执行支付。控制器因而仅需路由分发，不感知工厂与具体处理器的存在。</p>
 *
 * <p>未知渠道的判定与失败由工厂以业务异常形式抛出（最终经全局异常处理器统一转换为错误响应），
 * 本服务不做额外的渠道校验或异常吞咽，保持职责单一。</p>
 *
 * @since 1.0.0
 */
@Service
public class PaymentService {

    /**
     * 支付处理器工厂（工厂方法模式的工厂角色）。
     */
    @Resource
    private PaymentProcessorFactory paymentProcessorFactory;

    /**
     * 按渠道选取处理器并执行支付。
     *
     * <p>先由工厂依据渠道标识取得对应的支付处理器，再由该处理器基于支付上下文执行支付并返回结果。
     * 渠道为空或不受支持时，由工厂抛出业务异常，本方法不做拦截。</p>
     *
     * @param channel 支付渠道标识，如 {@code "wechat"}、{@code "alipay"}
     * @param ctx     支付请求上下文，承载订单号、金额、付款用户等支付要素
     * @return 支付结果，包含渠道、流水号、金额与完成时间等可观察信息
     */
    public PayResult pay(String channel, PaymentContext ctx) {
        PaymentProcessor processor = paymentProcessorFactory.create(channel);
        return processor.pay(ctx);
    }
}
