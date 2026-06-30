package com.example.patterns.structural.facade.controller;

import com.example.patterns.common.core.controller.BaseController;
import com.example.patterns.common.core.domain.AjaxResult;
import com.example.patterns.structural.facade.OrderPlacementFacade;
import com.example.patterns.structural.facade.domain.PlaceOrderRequest;
import com.example.patterns.structural.facade.domain.PlaceOrderResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 外观模式演示控制器。
 *
 * <p>对外暴露下单的 HTTP 演示入口，演示外观模式「以单一入口编排库存、优惠、支付多个子系统」
 * 的运行效果。控制器仅负责路由分发：接收并校验下单请求后直接委派
 * {@link OrderPlacementFacade}，自身不感知任何子系统、不承载任何业务逻辑。</p>
 *
 * <p>外观本身即统一入口，故控制器对其为「一行委派」。任一子系统失败均由外观抛出
 * {@code ServiceException}，经全局异常处理器统一转换为错误响应，控制器无需 try-catch。</p>
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("/pattern/facade")
public class OrderFacadeController extends BaseController {

    /**
     * 下单外观（外观模式的「外观」角色，复杂下单流程的统一入口）。
     */
    @Resource
    private OrderPlacementFacade orderPlacementFacade;

    /**
     * 下单。
     *
     * <p>由 {@code @Validated} 触发下单请求的声明式校验，校验通过后直接委派下单外观完成
     * 「扣减库存 → 计算优惠 → 发起支付」的整套流程，并以统一响应结构返回下单结果。</p>
     *
     * @param request 下单请求，含商品编码、购买数量、商品单价、下单用户与支付渠道
     * @return 携带 {@link PlaceOrderResult} 的统一成功响应
     */
    @PostMapping("/placeOrder")
    public AjaxResult placeOrder(@Validated @RequestBody PlaceOrderRequest request) {
        PlaceOrderResult result = orderPlacementFacade.placeOrder(request);
        return success(result);
    }
}
