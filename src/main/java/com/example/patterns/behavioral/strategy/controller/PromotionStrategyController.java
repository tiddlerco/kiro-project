package com.example.patterns.behavioral.strategy.controller;

import com.example.patterns.behavioral.strategy.domain.PromotionCalculateRequest;
import com.example.patterns.behavioral.strategy.domain.PromotionContext;
import com.example.patterns.behavioral.strategy.domain.PromotionResult;
import com.example.patterns.behavioral.strategy.service.PromotionCalculateService;
import com.example.patterns.common.core.controller.BaseController;
import com.example.patterns.common.core.domain.AjaxResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 策略模式演示控制器。
 *
 * <p>对外暴露促销计算的 HTTP 演示入口，演示策略模式「上下文按类型标识选取并委派具体策略」的
 * 运行效果。控制器仅负责路由分发：接收并校验请求、组装上下文、委派
 * {@link PromotionCalculateService} 计算并统一返回，不承载任何促销业务逻辑。</p>
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("/pattern/strategy")
public class PromotionStrategyController extends BaseController {

    /**
     * 促销计算上下文服务（策略模式的上下文角色）。
     */
    @Resource
    private PromotionCalculateService promotionCalculateService;

    /**
     * 计算促销优惠结果。
     *
     * <p>由 {@code @Validated} 触发请求对象的声明式校验，将请求映射为促销计算上下文后
     * 委派服务层按策略类型标识选取对应策略完成计算，并以统一响应结构返回计算结果。</p>
     *
     * @param request 促销计算请求，含策略类型标识、原始金额及各策略所需的可选参数
     * @return 携带 {@link PromotionResult} 的统一成功响应
     */
    @PostMapping("/calculate")
    public AjaxResult calculate(@Validated @RequestBody PromotionCalculateRequest request) {
        PromotionContext context = request.toContext();
        PromotionResult result = promotionCalculateService.calculate(request.getType(), context);
        return success(result);
    }
}
