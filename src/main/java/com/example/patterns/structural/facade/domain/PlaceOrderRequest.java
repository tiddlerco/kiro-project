package com.example.patterns.structural.facade.domain;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * 下单请求。
 *
 * <p>外观模式中由调用方传入「外观（Facade）」的下单领域请求对象，承载一次下单所需的
 * 全部业务要素：购买的商品、数量、单价、下单用户与支付渠道。调用方只需填充本对象并
 * 交给 {@link com.example.patterns.structural.facade.OrderPlacementFacade#placeOrder} ，
 * 即可完成「扣减库存 → 计算优惠 → 发起支付」的整套下单流程，而无需感知背后任何子系统。</p>
 *
 * <p>本类为纯数据对象（DTO），不承载任何业务逻辑；getter/setter 由 Lombok 的
 * {@link Data} 注解生成。金额采用 {@link BigDecimal} 表示，以避免浮点类型在金融
 * 计算中引入的精度误差。</p>
 *
 * @since 1.0.0
 */
@Data
public class PlaceOrderRequest {

    /**
     * 商品编码。
     *
     * <p>用于在库存子系统中定位目标商品并扣减其库存。</p>
     */
    @NotBlank(message = "商品编码不能为空")
    private String productCode;

    /**
     * 购买数量。
     *
     * <p>须为正整数，库存子系统据此扣减库存，金额计算据此与单价相乘。</p>
     */
    @Positive(message = "购买数量必须大于 0")
    private int quantity;

    /**
     * 商品单价（单位：元）。
     *
     * <p>采用 {@link BigDecimal} 以保证金额精度；与购买数量相乘得到订单原始金额。</p>
     */
    @NotNull(message = "商品单价不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "商品单价必须大于 0")
    private BigDecimal unitPrice;

    /**
     * 下单用户标识。
     *
     * <p>标识本次下单的付款人，透传至支付子系统作为付款用户。</p>
     */
    @NotBlank(message = "下单用户不能为空")
    private String buyerId;

    /**
     * 支付渠道标识。
     *
     * <p>如 {@code "wechat"}、{@code "alipay"}、{@code "balance"}，由支付子系统据此发起支付。</p>
     */
    @NotBlank(message = "支付渠道不能为空")
    private String payChannel;

    /**
     * 以更具表达力的静态工厂方式构造下单请求。
     *
     * @param productCode 商品编码
     * @param quantity    购买数量（须为正整数）
     * @param unitPrice   商品单价（单位：元）
     * @param buyerId     下单用户标识
     * @param payChannel  支付渠道标识
     * @return 各字段填充完成的下单请求对象
     */
    public static PlaceOrderRequest of(String productCode, int quantity, BigDecimal unitPrice,
                                       String buyerId, String payChannel) {
        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setProductCode(productCode);
        request.setQuantity(quantity);
        request.setUnitPrice(unitPrice);
        request.setBuyerId(buyerId);
        request.setPayChannel(payChannel);
        return request;
    }
}
