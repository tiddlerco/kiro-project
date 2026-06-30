package com.example.patterns.structural.proxy.controller;

import com.example.patterns.common.core.controller.BaseController;
import com.example.patterns.common.core.domain.AjaxResult;
import com.example.patterns.structural.proxy.ReportQueryService;
import com.example.patterns.structural.proxy.domain.ReportData;
import com.example.patterns.structural.proxy.domain.ReportQueryRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 代理模式演示控制器。
 *
 * <p>对外暴露报表查询的 HTTP 演示入口，演示代理模式「在不修改真实主题代码的前提下，由代理透明织入
 * 缓存与限流等横切逻辑」的运行效果。本控制器仅注入抽象主题接口 {@link ReportQueryService}，
 * 而无需感知缓存、限流的存在——Spring AOP 会自动以切面
 * {@link com.example.patterns.structural.proxy.CacheRateLimitAspect} 织入这些横切逻辑，
 * 这正是代理模式的核心演示点：调用方仅依赖抽象，对代理的增强行为无感知。</p>
 *
 * <p>控制器仅负责路由分发：接收并校验请求、委派服务执行查询并统一返回，不承载任何业务逻辑。</p>
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("/pattern/proxy")
public class ReportQueryController extends BaseController {

    /**
     * 报表查询服务（代理模式中的抽象主题角色）。
     *
     * <p>此处注入接口即可，运行期实际调用会被 Spring AOP 代理拦截并织入缓存与限流逻辑，
     * 控制器对该增强过程无感知。</p>
     */
    @Resource
    private ReportQueryService reportQueryService;

    /**
     * 查询报表数据。
     *
     * <p>由 {@code @Validated} 触发请求对象的声明式参数校验，随后委派报表查询服务执行查询并统一返回结果。
     * 以相同参数重复调用可观察缓存命中（返回结果的生成时间 {@code generatedAt} 保持不变，
     * 说明未重新执行真实计算）；短时间内高频调用可观察限流（超过阈值时由切面抛出业务异常，
     * 经全局异常处理器转换为错误响应返回）。</p>
     *
     * @param req 报表查询请求，承载报表类型、统计起止日期与可选地区维度
     * @return 携带 {@link ReportData} 的统一成功响应
     */
    @PostMapping("/queryReport")
    public AjaxResult queryReport(@Validated @RequestBody ReportQueryRequest req) {
        ReportData data = reportQueryService.query(req);
        return success(data);
    }
}
