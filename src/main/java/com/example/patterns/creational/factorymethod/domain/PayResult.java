package com.example.patterns.creational.factorymethod.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付结果。
 *
 * <p>承载某个支付处理器执行支付后的可观察结果，包括是否成功、所用渠道、
 * 订单号、渠道生成的支付流水号、实际支付金额、结果描述与完成时间。
 * 作为 {@link com.example.patterns.creational.factorymethod.PaymentProcessor#pay}
 * 的返回值，供调用方据此判断支付是否成功并展示结果。</p>
 *
 * <p>本类为纯数据对象（DTO），getter/setter 由 Lombok 的 {@link Data} 注解生成；
 * 同时提供 {@link #success} 静态工厂方法，便于各支付处理器以表达力更强的方式
 * 构造成功结果，避免逐字段手工装配。</p>
 *
 * @since 1.0.0
 */
@Data
public class PayResult {

    /**
     * 是否支付成功。
     */
    private boolean success;

    /**
     * 支付渠道标识。
     *
     * <p>取自实际处理本次支付的处理器所声明的渠道标识，如 {@code "wechat"}、{@code "alipay"}。</p>
     */
    private String channel;

    /**
     * 订单号。
     */
    private String orderNo;

    /**
     * 支付流水号。
     *
     * <p>由具体支付渠道生成的唯一交易凭证编号，用于对账与问题追溯。</p>
     */
    private String transactionId;

    /**
     * 实际支付金额（单位：元）。
     */
    private BigDecimal amount;

    /**
     * 结果描述信息。
     */
    private String message;

    /**
     * 支付完成时间。
     */
    private LocalDateTime payTime;

    /**
     * 构建一个表示「支付成功」的结果，并自动填充完成时间为当前时刻。
     *
     * @param channel       实际处理支付的渠道标识
     * @param orderNo       订单号
     * @param transactionId 渠道生成的支付流水号
     * @param amount        实际支付金额
     * @param message       结果描述信息
     * @return 表示支付成功且各字段填充完成的支付结果
     */
    public static PayResult success(String channel, String orderNo, String transactionId,
                                    BigDecimal amount, String message) {
        PayResult result = new PayResult();
        result.setSuccess(true);
        result.setChannel(channel);
        result.setOrderNo(orderNo);
        result.setTransactionId(transactionId);
        result.setAmount(amount);
        result.setMessage(message);
        result.setPayTime(LocalDateTime.now());
        return result;
    }
}
