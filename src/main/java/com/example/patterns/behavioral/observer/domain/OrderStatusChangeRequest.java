package com.example.patterns.behavioral.observer.domain;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 订单状态变更请求。
 *
 * <p>观察者模式演示接口 {@code POST /pattern/observer/changeOrderStatus} 的入参对象，承载
 * 一次订单状态变更所需的全部输入：订单号、变更前状态、变更后状态与操作人。其上的校验注解供
 * 控制器以 {@code @Validated} 触发声明式参数校验，避免在控制器中手写 if 校验逻辑
 * （与项目统一的请求对象校验规范一致）。</p>
 *
 * <p>本类为纯数据对象（DTO），不承载任何业务逻辑；getter/setter 由 Lombok 的 {@link Data}
 * 注解生成。状态字段采用 {@link OrderStatus} 枚举而非裸字符串，可在反序列化阶段即约束取值
 * 范围，规避非法状态值流入后续监听者处理。</p>
 *
 * @since 1.0.0
 */
@Data
public class OrderStatusChangeRequest {

    /**
     * 发生状态变更的订单号。
     *
     * <p>用于标识本次状态变更所属的订单，必填。</p>
     */
    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    /**
     * 变更前的订单状态。
     *
     * <p>表示订单在本次变更之前所处的状态，必填。</p>
     */
    @NotNull(message = "变更前状态不能为空")
    private OrderStatus oldStatus;

    /**
     * 变更后的订单状态。
     *
     * <p>表示订单在本次变更之后所处的状态，必填；各监听者据此决定自身的响应行为。</p>
     */
    @NotNull(message = "变更后状态不能为空")
    private OrderStatus newStatus;

    /**
     * 触发本次状态变更的操作人标识。
     *
     * <p>如客服工号、系统任务名，用于追溯变更来源，必填。</p>
     */
    @NotBlank(message = "操作人不能为空")
    private String operator;
}
