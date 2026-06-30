package com.example.patterns.creational.factorymethod.domain;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 支付请求。
 *
 * <p>工厂方法模式演示接口 {@code POST /pattern/factory/pay} 的入参对象，承载一次支付所需的
 * 全部输入：支付渠道标识及订单号、金额、付款用户、交易标题等支付要素。其上的校验注解供控制器以
 * {@code @Validated} 触发声明式参数校验，避免在控制器中手写 if 校验逻辑（与项目统一的请求
 * 对象校验规范一致）。</p>
 *
 * <p>本类为纯数据对象（DTO），不承载任何支付业务逻辑；getter/setter 由 Lombok 的
 * {@link Data} 注解生成。金额采用 {@link BigDecimal} 表示，避免浮点类型在金额场景中引入
 * 精度误差。通过 {@link #toContext()} 映射为 {@link PaymentContext} 后交由工厂选取的支付
 * 处理器执行，使控制器保持仅路由分发的职责。</p>
 *
 * @since 1.0.0
 */
@Data
public class PaymentPayRequest {

    /**
     * 支付渠道标识。
     *
     * <p>工厂据此选取对应的支付处理器，如 {@code "wechat"}、{@code "alipay"}，必填；
     * 未知渠道由工厂抛出业务异常并经全局异常处理器返回错误。</p>
     */
    @NotBlank(message = "支付渠道标识不能为空")
    private String channel;

    /**
     * 订单号。
     *
     * <p>标识本次支付对应的业务订单，必填。</p>
     */
    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    /**
     * 支付金额（单位：元）。
     *
     * <p>采用 {@link BigDecimal} 以保证金额精度，必填且业务上要求大于 0。</p>
     */
    @NotNull(message = "支付金额不能为空")
    @DecimalMin(value = "0.01", message = "支付金额必须大于 0")
    private BigDecimal amount;

    /**
     * 付款用户标识。
     *
     * <p>标识发起本次支付的用户，必填。</p>
     */
    @NotBlank(message = "付款用户标识不能为空")
    private String userId;

    /**
     * 交易标题。
     *
     * <p>对本次支付内容的简要描述，如「商品名称」或「订单摘要」，可选。</p>
     */
    private String subject;

    /**
     * 将当前请求对象映射为支付上下文。
     *
     * <p>仅做支付要素字段的一对一搬运（渠道标识用于工厂选取处理器，不属于上下文字段），
     * 不承载任何支付业务判断，使控制器保持仅路由分发的职责。</p>
     *
     * @return 与本请求支付要素一致的支付上下文
     */
    public PaymentContext toContext() {
        PaymentContext context = new PaymentContext();
        context.setOrderNo(orderNo);
        context.setAmount(amount);
        context.setUserId(userId);
        context.setSubject(subject);
        return context;
    }
}
