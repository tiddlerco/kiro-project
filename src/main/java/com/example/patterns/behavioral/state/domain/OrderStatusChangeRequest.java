package com.example.patterns.behavioral.state.domain;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 订单状态流转请求。
 *
 * <p>状态模式演示接口 {@code POST /pattern/order/changeStatus} 的入参对象，承载一次订单状态
 * 流转所需的全部输入：目标订单主键 id 与待执行的订单动作标识。其上的校验注解供控制器以
 * {@code @Validated} 触发声明式参数校验，避免在控制器中手写 if 校验逻辑（与项目统一的请求
 * 对象校验规范一致）。</p>
 *
 * <p>本类为纯数据对象（DTO），不承载任何状态流转判定逻辑；状态流转的合法性判定完全交由状态机
 * （各 {@code OrderState} 实现）承载，非法流转由状态机抛出非法状态流转异常并经全局异常处理器
 * 转换为错误响应。getter/setter 由 Lombok 的 {@link Data} 注解生成。</p>
 *
 * @since 1.0.0
 */
@Data
public class OrderStatusChangeRequest {

    /**
     * 目标订单主键 id。
     *
     * <p>服务层据此查询订单并构建状态上下文，必填。</p>
     */
    @NotNull(message = "订单 id 不能为空")
    private Long orderId;

    /**
     * 订单动作标识。
     *
     * <p>取值为 {@code pay}（支付）/{@code ship}（发货）/{@code complete}（完成）/{@code cancel}（取消），
     * 不区分大小写，必填。动作在当前状态下是否合法由状态机判定。</p>
     */
    @NotBlank(message = "订单动作不能为空")
    private String action;
}
