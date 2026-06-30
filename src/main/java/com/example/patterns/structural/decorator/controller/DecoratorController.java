package com.example.patterns.structural.decorator.controller;

import com.example.patterns.common.core.controller.BaseController;
import com.example.patterns.common.core.domain.AjaxResult;
import com.example.patterns.structural.decorator.domain.DecoratorSendRequest;
import com.example.patterns.structural.decorator.domain.NotifyContent;
import com.example.patterns.structural.decorator.domain.SendResult;
import com.example.patterns.structural.decorator.service.DecoratorNotifyService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 装饰器模式演示控制器。
 *
 * <p>对外暴露装饰器模式的演示入口：接收发送请求并按指定能力列表叠加签名、加密、日志等增强能力
 * 后发送通知。控制器仅负责路由分发（接收参数 → 调用服务 → 返回结果），装饰链的组装逻辑全部下沉
 * 至 {@link DecoratorNotifyService}。</p>
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("/pattern/decorator")
public class DecoratorController extends BaseController {

    /**
     * 装饰器通知组装服务，负责按能力列表组装装饰链并发送通知。
     */
    @Resource
    private DecoratorNotifyService decoratorNotifyService;

    /**
     * 发送通知并按指定能力列表叠加增强能力。
     *
     * <p>将请求字段转换为通知内容，连同待叠加能力列表交由服务层组装装饰链并发送，最终返回包含
     * 最终正文与已应用能力的发送结果。</p>
     *
     * @param request 装饰器通知发送请求，已通过 {@code @Validated} 完成接收人/标题/正文非空校验
     * @return 携带发送结果 {@link SendResult} 的成功响应
     */
    @PostMapping("/send")
    public AjaxResult send(@Validated @RequestBody DecoratorSendRequest request) {
        NotifyContent content = new NotifyContent();
        content.setReceiver(request.getReceiver());
        content.setTitle(request.getTitle());
        content.setBody(request.getBody());
        SendResult result = decoratorNotifyService.send(content, request.getCapabilities());
        return success(result);
    }
}
