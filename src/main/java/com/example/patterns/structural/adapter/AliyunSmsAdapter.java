package com.example.patterns.structural.adapter;

import com.example.patterns.structural.adapter.domain.SmsRequest;
import com.example.patterns.structural.adapter.domain.SmsSendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

/**
 * 阿里云短信适配器。
 *
 * <p>适配器模式中的「适配器（Adapter）」角色之一，实现统一目标接口 {@link SmsSender}，
 * 持有被适配者 {@link AliyunSmsClient}（经 Spring 容器注入），负责把统一的
 * {@link SmsRequest} 适配到阿里云 SDK 的差异化签名
 * {@code sendSms(signName, phoneNumbers, templateParam)} 上，
 * 并把阿里云返回的回执标识归一化为统一的 {@link SmsSendResult}。</p>
 *
 * <p>本适配器以「对象组合」方式持有被适配者，而非通过继承复用其能力，
 * 符合「组合优于继承」（需求 8.3）；新增服务商不影响本类（开闭原则）。</p>
 *
 * @since 1.0.0
 */
@Component
public class AliyunSmsAdapter implements SmsSender {

    /**
     * 阿里云服务商标识。
     */
    private static final String VENDOR_ALIYUN = "aliyun";

    /**
     * 短信签名缺省值。
     *
     * <p>当统一请求未显式携带签名时，采用本默认签名，以满足阿里云发送时必须携带签名的要求。</p>
     */
    private static final String DEFAULT_SIGN_NAME = "【设计模式商城】";

    /**
     * 被适配的阿里云短信客户端（第三方 SDK），由 Spring 容器注入。
     */
    @Resource
    private AliyunSmsClient aliyunSmsClient;

    /**
     * 发送短信。
     *
     * <p>将统一请求的接收号码与正文，连同解析得到的短信签名，适配为阿里云 SDK 的
     * {@code sendSms} 三段式调用，再把其返回的回执标识封装为统一发送结果。</p>
     *
     * @param req 统一短信发送请求，承载接收手机号、短信内容与签名等要素
     * @return 表示阿里云短信发送成功的统一发送结果
     */
    @Override
    public SmsSendResult send(SmsRequest req) {
        String signName = resolveSignName(req.getSignName());
        String bizId = aliyunSmsClient.sendSms(signName, req.getPhone(), req.getContent());
        return SmsSendResult.success(VENDOR_ALIYUN, req.getPhone(), bizId, "阿里云短信发送成功");
    }

    /**
     * 返回阿里云服务商标识。
     *
     * @return 服务商标识 {@value #VENDOR_ALIYUN}
     */
    @Override
    public String vendor() {
        return VENDOR_ALIYUN;
    }

    /**
     * 解析最终使用的短信签名。
     *
     * <p>统一请求显式携带签名时优先使用之，否则回退为默认签名 {@value #DEFAULT_SIGN_NAME}。</p>
     *
     * @param signName 统一请求中携带的短信签名，可能为空
     * @return 实际用于调用阿里云 SDK 的短信签名，恒为非空
     */
    private String resolveSignName(String signName) {
        return StringUtils.hasText(signName) ? signName : DEFAULT_SIGN_NAME;
    }
}
