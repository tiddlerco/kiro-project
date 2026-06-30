package com.example.patterns.structural.adapter;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 阿里云短信客户端（模拟第三方 SDK）。
 *
 * <p>适配器模式中的「被适配者（Adaptee）」角色之一，模拟阿里云短信服务的官方 SDK。
 * 其方法签名由阿里云一方约定、不可更改，与统一目标接口 {@link SmsSender} 及其它服务商
 * SDK（如 {@link TencentSmsClient}）的签名均不相同，故需由 {@link AliyunSmsAdapter}
 * 进行适配。</p>
 *
 * <p>本类仅为演示而模拟回执生成，不进行真实网络调用。注册为 Spring 组件，是为了能由
 * 容器注入到对应适配器中（满足「依赖经容器注入而非内部 new」的约束），真实工程中
 * 第三方 SDK 客户端通常改由 {@code @Bean} 配置类注册。</p>
 *
 * @since 1.0.0
 */
@Component
public class AliyunSmsClient {

    /**
     * 阿里云短信回执标识（BizId）前缀。
     */
    private static final String BIZ_ID_PREFIX = "ALI";

    /**
     * 发送短信（模拟阿里云 SDK 的 {@code sendSms} 接口）。
     *
     * <p>该方法签名刻意采用阿里云风格：以短信签名、接收号码、模板参数三段式传参，
     * 与腾讯云等其它服务商的签名形成差异，以凸显适配器存在的必要性。</p>
     *
     * @param signName      短信签名，阿里云要求发送时显式携带
     * @param phoneNumbers  接收短信的手机号
     * @param templateParam 短信模板参数，此处承载短信正文内容
     * @return 阿里云返回的短信回执标识（BizId）
     */
    public String sendSms(String signName, String phoneNumbers, String templateParam) {
        return BIZ_ID_PREFIX + UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }
}
