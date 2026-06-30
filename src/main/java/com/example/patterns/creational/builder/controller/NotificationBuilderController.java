package com.example.patterns.creational.builder.controller;

import com.example.patterns.common.core.controller.BaseController;
import com.example.patterns.common.core.domain.AjaxResult;
import com.example.patterns.creational.builder.domain.NotificationBuildRequest;
import com.example.patterns.creational.builder.service.NotificationBuildService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 建造者模式演示控制器。
 *
 * <p>对外暴露通知消息构建的 HTTP 演示入口，演示建造者模式「经统一入口分步设置必选与可选部件、
 * 收尾时校验必选部件并产出不可变产品」的运行效果。控制器仅负责路由分发：接收并校验请求后委派
 * {@link NotificationBuildService} 完成构建编排并统一返回，不承载任何构建业务逻辑（落实约束 C6）。</p>
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("/pattern/builder")
public class NotificationBuilderController extends BaseController {

    /**
     * 通知消息构建服务（建造者模式的构建编排角色）。
     */
    @Resource
    private NotificationBuildService notificationBuildService;

    /**
     * 构建通知消息。
     *
     * <p>由 {@code @Validated} 触发请求对象的声明式校验，校验通过后委派构建服务依据请求分步装配
     * 并构建出不可变的通知消息，以统一响应结构返回构建结果；必选部件缺失等业务失败由全局异常
     * 处理器统一转换为错误响应。</p>
     *
     * @param request 构建通知消息请求，含必选「接收人」与可选「标题 / 正文 / 附件 / 优先级」
     * @return 携带构建完成的通知消息的统一成功响应
     */
    @PostMapping("/buildNotification")
    public AjaxResult buildNotification(@Validated @RequestBody NotificationBuildRequest request) {
        return success(notificationBuildService.buildNotification(request));
    }
}
