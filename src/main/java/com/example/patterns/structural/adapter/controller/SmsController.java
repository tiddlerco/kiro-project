package com.example.patterns.structural.adapter.controller;

import com.example.patterns.common.core.controller.BaseController;
import com.example.patterns.common.core.domain.AjaxResult;
import com.example.patterns.structural.adapter.domain.SmsRequest;
import com.example.patterns.structural.adapter.service.SmsService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 适配器模式演示控制器。
 *
 * <p>对外暴露统一短信发送的 HTTP 演示入口，演示适配器模式「以统一接口对接多家接口签名互不相同的
 * 第三方短信服务商」的运行效果。控制器仅负责路由分发：触发请求参数校验后委派 {@link SmsService}
 * 按服务商标识路由到对应适配器完成发送，不承载任何短信发送业务逻辑。</p>
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("/pattern/adapter")
public class SmsController extends BaseController {

    /**
     * 短信发送服务（适配器模式的调用方角色，按服务商标识路由到对应适配器）。
     */
    @Resource
    private SmsService smsService;

    /**
     * 发送短信。
     *
     * <p>由 {@code @Validated} 触发请求对象的声明式校验，委派短信发送服务按请求携带的服务商标识
     * 选取对应适配器完成发送，并以统一响应结构返回发送结果。</p>
     *
     * @param req 统一短信发送请求，含目标服务商、接收手机号、短信内容与可选签名
     * @return 携带 {@link com.example.patterns.structural.adapter.domain.SmsSendResult} 的统一成功响应
     */
    @PostMapping("/sendSms")
    public AjaxResult sendSms(@Validated @RequestBody SmsRequest req) {
        return success(smsService.send(req));
    }
}
