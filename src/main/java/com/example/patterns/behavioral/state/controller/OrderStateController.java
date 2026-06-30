package com.example.patterns.behavioral.state.controller;

import com.example.patterns.behavioral.state.domain.OrderStatusChangeRequest;
import com.example.patterns.behavioral.state.entity.OrderEntity;
import com.example.patterns.behavioral.state.service.OrderStateService;
import com.example.patterns.common.core.controller.BaseController;
import com.example.patterns.common.core.domain.AjaxResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 状态模式演示控制器。
 *
 * <p>对外暴露订单状态流转的 HTTP 演示入口，演示状态模式「同一动作在不同状态下表现不同行为，
 * 合法流转转入目标状态、非法流转被拒绝且状态不变」的运行效果。控制器仅负责路由分发：接收并
 * 校验请求、委派 {@link OrderStateService} 驱动状态机完成流转并统一返回，不承载任何状态判定
 * 逻辑。非法流转由状态机抛出非法状态流转异常，经全局异常处理器统一转换为错误响应，控制器不做
 * try-catch 处理。</p>
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("/pattern/order")
public class OrderStateController extends BaseController {

    /**
     * 订单状态机组装与流转服务（状态模式中「上下文」的装配与统一入口）。
     */
    @Resource
    private OrderStateService orderStateService;

    /**
     * 执行订单状态流转。
     *
     * <p>由 {@code @Validated} 触发请求对象的声明式校验，校验通过后委派状态服务依据动作标识
     * 驱动订单从当前状态流转至目标状态，并以统一响应结构返回流转后的订单实体。合法流转将转入
     * 目标状态，非法流转由状态机抛出非法状态流转异常并经全局异常处理器返回错误（本方法不处理）。</p>
     *
     * @param request 订单状态流转请求，含订单 id 与动作标识（pay/ship/complete/cancel）
     * @return 携带流转后的 {@link OrderEntity} 的统一成功响应
     */
    @PostMapping("/changeStatus")
    public AjaxResult changeStatus(@Validated @RequestBody OrderStatusChangeRequest request) {
        OrderEntity order = orderStateService.changeStatus(request.getOrderId(), request.getAction());
        return success(order);
    }
}
