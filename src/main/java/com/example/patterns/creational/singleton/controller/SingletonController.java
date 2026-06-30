package com.example.patterns.creational.singleton.controller;

import com.example.patterns.common.core.controller.BaseController;
import com.example.patterns.common.core.domain.AjaxResult;
import com.example.patterns.creational.singleton.domain.SingletonComparisonResult;
import com.example.patterns.creational.singleton.domain.SingletonConfigRequest;
import com.example.patterns.creational.singleton.service.SingletonDemoService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 单例模式演示控制器。
 *
 * <p>对外暴露单例模式的 HTTP 演示入口，演示「Spring 单例 Bean」与「经典手写单例」两种实现：
 * 既支持向全局配置管理器（Spring 单例）写入配置，又支持对比两种单例「多次获取是否返回同一实例
 * （引用相等）」。控制器仅负责路由分发：接收并校验请求、委派 {@link SingletonDemoService}
 * 执行逻辑并统一返回，不承载任何业务逻辑。</p>
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("/pattern/singleton")
public class SingletonController extends BaseController {

    /**
     * 单例模式演示服务（承载两种单例实现的读写与对比逻辑）。
     */
    @Resource
    private SingletonDemoService singletonDemoService;

    /**
     * 设置一项全局配置。
     *
     * <p>由 {@code @Validated} 触发请求对象的声明式校验，校验通过后委派服务向全局配置管理器
     * （Spring 单例 Bean）写入配置，演示「容器托管的单例全应用共享同一份配置」。</p>
     *
     * @param request 全局配置设置请求，含配置键与配置值
     * @return 表示设置成功的统一响应
     */
    @PostMapping("/setConfig")
    public AjaxResult setConfig(@Validated @RequestBody SingletonConfigRequest request) {
        singletonDemoService.setGlobalConfig(request.getKey(), request.getValue());
        return success();
    }

    /**
     * 对比两种单例实现，验证多次获取是否返回同一实例（引用相等）。
     *
     * <p>委派服务分别对「Spring 单例 Bean」与「经典手写单例」执行多次获取并以引用比较判定，
     * 返回汇总二者获取方式及引用相等结论的对比结果。</p>
     *
     * @return 携带 {@link SingletonComparisonResult} 的统一成功响应
     */
    @GetMapping("/sameInstance")
    public AjaxResult sameInstance() {
        SingletonComparisonResult result = singletonDemoService.compareSingletonAcquisition();
        return success(result);
    }
}
