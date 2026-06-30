package com.example.patterns.creational.factorymethod.domain;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 支付请求上下文。
 *
 * <p>承载一次支付请求所需的业务字段（订单号、金额、付款用户、交易标题等），
 * 作为 {@link com.example.patterns.creational.factorymethod.PaymentProcessor#pay} 的入参，
 * 在调用方与具体支付处理器之间传递支付要素。</p>
 *
 * <p>本类为纯数据对象（DTO），不承载任何支付业务逻辑；getter/setter 由 Lombok
 * 的 {@link Data} 注解生成。金额采用 {@link BigDecimal} 表示，避免使用浮点类型
 * 带来的精度误差（金融场景的基本要求）。</p>
 *
 * @since 1.0.0
 */
@Data
public class PaymentContext {

    /**
     * 订单号。
     *
     * <p>用于标识本次支付对应的业务订单，体现在支付结果中以便追溯。</p>
     */
    private String orderNo;

    /**
     * 支付金额（单位：元）。
     *
     * <p>采用 {@link BigDecimal} 以保证金额精度，避免浮点运算误差。</p>
     */
    private BigDecimal amount;

    /**
     * 付款用户标识。
     */
    private String userId;

    /**
     * 交易标题。
     *
     * <p>对本次支付内容的简要描述，如「商品名称」或「订单摘要」。</p>
     */
    private String subject;
}
