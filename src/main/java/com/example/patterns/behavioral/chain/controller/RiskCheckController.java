package com.example.patterns.behavioral.chain.controller;

import com.example.patterns.behavioral.chain.domain.RiskCheckRequest;
import com.example.patterns.behavioral.chain.domain.RiskCheckResult;
import com.example.patterns.behavioral.chain.domain.RiskContext;
import com.example.patterns.behavioral.chain.service.RiskRuleChain;
import com.example.patterns.common.core.controller.BaseController;
import com.example.patterns.common.core.domain.AjaxResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 责任链模式演示控制器。
 *
 * <p>对外暴露风控校验的 HTTP 演示入口，演示责任链模式「请求沿有序规则链依次传递，直至被某一
 * 节点拦截而短路，或顺利通过全部节点」的运行效果。控制器仅负责路由分发：接收并校验请求、
 * 组装风控上下文、委派 {@link RiskRuleChain} 驱动校验并统一返回，不承载任何风控业务逻辑。</p>
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("/pattern/chain")
public class RiskCheckController extends BaseController {

    /**
     * 风控规则链装配与驱动服务（责任链模式的链驱动角色）。
     */
    @Resource
    private RiskRuleChain riskRuleChain;

    /**
     * 执行风控校验。
     *
     * <p>由 {@code @Validated} 触发请求对象的声明式校验，将请求映射为风控校验上下文后委派
     * 责任链驱动各规则节点依次校验，并以统一响应结构返回校验结果（通过或被某节点拦截）。</p>
     *
     * @param request 风控校验请求，含用户标识、交易金额与近期交易次数
     * @return 携带 {@link RiskCheckResult} 的统一成功响应
     */
    @PostMapping("/riskCheck")
    public AjaxResult riskCheck(@Validated @RequestBody RiskCheckRequest request) {
        RiskContext context = request.toContext();
        RiskCheckResult result = riskRuleChain.check(context);
        return success(result);
    }
}
