package com.example.patterns.structural.adapter;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 腾讯云短信客户端（模拟第三方 SDK）。
 *
 * <p>适配器模式中的「被适配者（Adaptee）」角色之一，模拟腾讯云短信服务的官方 SDK。
 * 其方法签名由腾讯云一方约定、不可更改，与统一目标接口 {@link SmsSender} 及其它服务商
 * SDK（如 {@link AliyunSmsClient}）的签名均不相同，故需由 {@link TencentSmsAdapter}
 * 进行适配。</p>
 *
 * <p>本类仅为演示而模拟回执生成，不进行真实网络调用。注册为 Spring 组件，是为了能由
 * 容器注入到对应适配器中（满足「依赖经容器注入而非内部 new」的约束），真实工程中
 * 第三方 SDK 客户端通常改由 {@code @Bean} 配置类注册。</p>
 *
 * @since 1.0.0
 */
@Component
public class TencentSmsClient {

    /**
     * 腾讯云短信流水号（serialNo）前缀。
     */
    private static final String SERIAL_NO_PREFIX = "TX";

    /**
     * 推送短信消息（模拟腾讯云 SDK 的 {@code pushMessage} 接口）。
     *
     * <p>该方法签名刻意采用腾讯云风格：以手机号、短信内容、应用 ID 三段式传参，
     * 其中应用 ID 为 {@code int} 类型，与阿里云等其它服务商的签名形成差异，
     * 以凸显适配器存在的必要性。</p>
     *
     * @param mobile  接收短信的手机号
     * @param content 短信内容
     * @param appId   腾讯云短信应用 ID，由服务商在接入时分配
     * @return 腾讯云返回的短信流水号（serialNo）
     */
    public String pushMessage(String mobile, String content, int appId) {
        return SERIAL_NO_PREFIX + appId + "_" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }
}
