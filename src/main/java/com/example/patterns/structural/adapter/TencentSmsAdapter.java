package com.example.patterns.structural.adapter;

import com.example.patterns.structural.adapter.domain.SmsRequest;
import com.example.patterns.structural.adapter.domain.SmsSendResult;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 腾讯云短信适配器。
 *
 * <p>适配器模式中的「适配器（Adapter）」角色之一，实现统一目标接口 {@link SmsSender}，
 * 持有被适配者 {@link TencentSmsClient}（经 Spring 容器注入），负责把统一的
 * {@link SmsRequest} 适配到腾讯云 SDK 的差异化签名
 * {@code pushMessage(mobile, content, appId)} 上，
 * 并把腾讯云返回的流水号归一化为统一的 {@link SmsSendResult}。</p>
 *
 * <p>统一请求并不携带腾讯云特有的应用 ID，由本适配器补齐该差异化参数，
 * 这正体现了适配器「填平统一接口与目标 SDK 之间参数鸿沟」的职责。
 * 本适配器以「对象组合」方式持有被适配者，符合「组合优于继承」（需求 8.3）。</p>
 *
 * @since 1.0.0
 */
@Component
public class TencentSmsAdapter implements SmsSender {

    /**
     * 腾讯云服务商标识。
     */
    private static final String VENDOR_TENCENT = "tencent";

    /**
     * 腾讯云短信应用 ID。
     *
     * <p>模拟接入腾讯云短信时分配的应用 ID，为统一请求所不具备、而目标 SDK 必需的差异化参数，
     * 由适配器在调用时补齐。</p>
     */
    private static final int SMS_APP_ID = 1400000001;

    /**
     * 被适配的腾讯云短信客户端（第三方 SDK），由 Spring 容器注入。
     */
    @Resource
    private TencentSmsClient tencentSmsClient;

    /**
     * 发送短信。
     *
     * <p>将统一请求的接收号码与正文，连同适配器补齐的应用 ID，适配为腾讯云 SDK 的
     * {@code pushMessage} 调用，再把其返回的流水号封装为统一发送结果。</p>
     *
     * @param req 统一短信发送请求，承载接收手机号与短信内容等要素
     * @return 表示腾讯云短信发送成功的统一发送结果
     */
    @Override
    public SmsSendResult send(SmsRequest req) {
        String serialNo = tencentSmsClient.pushMessage(req.getPhone(), req.getContent(), SMS_APP_ID);
        return SmsSendResult.success(VENDOR_TENCENT, req.getPhone(), serialNo, "腾讯云短信发送成功");
    }

    /**
     * 返回腾讯云服务商标识。
     *
     * @return 服务商标识 {@value #VENDOR_TENCENT}
     */
    @Override
    public String vendor() {
        return VENDOR_TENCENT;
    }
}
