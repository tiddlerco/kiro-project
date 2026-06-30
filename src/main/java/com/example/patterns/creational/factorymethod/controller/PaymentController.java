package com.example.patterns.creational.factorymethod.controller;

import com.example.patterns.common.core.controller.BaseController;
import com.example.patterns.common.core.domain.AjaxResult;
import com.example.patterns.creational.factorymethod.domain.PayResult;
import com.example.patterns.creational.factorymethod.domain.PaymentContext;
import com.example.patterns.creational.factorymethod.domain.PaymentPayRequest;
import com.example.patterns.creational.factorymethod.service.PaymentService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 工厂方法模式演示控制器。
 *
 * <p>对外暴露按渠道支付的 HTTP 演示入口，演示工厂方法模式「由工厂依据渠道标识创建对应支付处理器，
 * 调用方仅依赖抽象接口与渠道标识」的运行效果。控制器仅负责路由分发：接收并校验请求、组装支付
 * 上下文、委派 {@link PaymentService} 完成支付并统一返回，不承载任何支付业务逻辑。</p>
 *
 * <p>未知渠道由工厂抛出业务异常并经全局异常处理器统一返回错误响应，控制器不做 try-catch 处理。</p>
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("/pattern/factory")
public class PaymentController extends BaseController {

    /**
     * 支付服务（封装按渠道选处理器并支付的业务动作）。
     */
    @Resource
    private PaymentService paymentService;

    /**
     * 执行支付。
     *
     * <p>由 {@code @Validated} 触发请求对象的声明式校验，将请求映射为支付上下文后委派支付服务
     * 按渠道选取处理器并执行支付，以统一响应结构返回支付结果。</p>
     *
     * @param request 支付请求，含支付渠道标识及订单号、金额、付款用户、交易标题等支付要素
     * @return 携带 {@link PayResult} 的统一成功响应
     */
    @PostMapping("/pay")
    public AjaxResult pay(@Validated @RequestBody PaymentPayRequest request) {
        PaymentContext context = request.toContext();
        PayResult result = paymentService.pay(request.getChannel(), context);
        return success(result);
    }
}
