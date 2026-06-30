package com.example.patterns.structural.facade.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 下单结果。
 *
 * <p>外观模式中「外观（Facade）」编排库存、优惠、支付三个子系统后向调用方返回的可观察结果，
 * 完整呈现一次成功下单的全貌：订单号、商品与数量、原始金额、优惠金额、实付金额、
 * 扣减库存后的剩余量、所用支付渠道与支付流水号、下单完成时间。调用方据此即可了解整笔
 * 下单的处理结果，无需再去探查任何子系统的内部状态（迪米特法则）。</p>
 *
 * <p>本类为纯数据对象（DTO），getter/setter 由 Lombok 的 {@link Data} 注解生成；
 * 各金额字段采用 {@link BigDecimal} 以保证精度。</p>
 *
 * @since 1.0.0
 */
@Data
public class PlaceOrderResult {

    /**
     * 订单号。
     *
     * <p>由外观在下单成功后生成的唯一业务单据编号，用于后续追溯。</p>
     */
    private String orderNo;

    /**
     * 商品编码。
     */
    private String productCode;

    /**
     * 购买数量。
     */
    private int quantity;

    /**
     * 订单原始金额（单位：元）。
     *
     * <p>即「单价 × 数量」，为计算优惠前的应付金额。</p>
     */
    private BigDecimal originalAmount;

    /**
     * 优惠金额（单位：元）。
     *
     * <p>由优惠子系统依据订单原始金额计算得出，区间为 {@code [0, originalAmount]}。</p>
     */
    private BigDecimal discountAmount;

    /**
     * 实付金额（单位：元）。
     *
     * <p>即「原始金额 − 优惠金额」，为支付子系统实际收取的金额。</p>
     */
    private BigDecimal payableAmount;

    /**
     * 扣减库存后的剩余库存。
     *
     * <p>由库存子系统在成功扣减后返回，反映本次下单后该商品的可售余量。</p>
     */
    private int remainingStock;

    /**
     * 支付渠道标识。
     *
     * <p>本次下单实际使用的支付渠道，如 {@code "wechat"}、{@code "alipay"}。</p>
     */
    private String payChannel;

    /**
     * 支付流水号。
     *
     * <p>由支付子系统生成的唯一交易凭证编号，用于对账与问题追溯。</p>
     */
    private String transactionId;

    /**
     * 下单完成时间。
     */
    private LocalDateTime placeTime;
}
