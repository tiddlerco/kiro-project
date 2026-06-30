package com.example.patterns.behavioral.templatemethod.controller;

import com.example.patterns.behavioral.templatemethod.domain.ReconcileRequest;
import com.example.patterns.behavioral.templatemethod.service.ReconcileService;
import com.example.patterns.common.core.controller.BaseController;
import com.example.patterns.common.core.domain.AjaxResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 模板方法模式演示控制器。
 *
 * <p>对外提供模板方法模式（企业对账场景）的 HTTP 演示入口，仅承担路由分发职责：
 * 接收对账请求、调用 {@link ReconcileService} 完成「按渠道选模板并触发对账」，
 * 返回统一响应结果。具体的渠道路由与对账编排逻辑均下沉至 Service 与对账模板，
 * 控制器不含任何业务判断。</p>
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("/pattern/template")
public class ReconcileController extends BaseController {

    /**
     * 对账渠道路由服务。
     */
    @Resource
    private ReconcileService reconcileService;

    /**
     * 触发指定渠道的对账。
     *
     * <p>接收携带渠道标识的对账请求，经声明式校验后委托
     * {@link ReconcileService#reconcile(String)} 按渠道路由到对应模板执行对账，
     * 并将对账报告封装为成功响应返回。</p>
     *
     * @param request 对账请求对象，其中渠道标识不能为空
     * @return 携带对账报告的成功响应结果
     */
    @PostMapping("/reconcile")
    public AjaxResult reconcile(@Validated @RequestBody ReconcileRequest request) {
        return success(reconcileService.reconcile(request.getChannel()));
    }
}
