package com.example.patterns.structural.adapter;

import com.example.patterns.structural.adapter.domain.SmsRequest;
import com.example.patterns.structural.adapter.domain.SmsSendResult;

/**
 * 短信发送器。
 *
 * <p>适配器模式中的「目标接口（Target）」角色，定义调用方所期望的、与服务商无关的
 * 统一短信发送能力。各第三方短信服务商的 SDK（{@link AliyunSmsClient}、
 * {@link TencentSmsClient} 等「被适配者 Adaptee」）方法签名互不相同，由各自的适配器
 * （{@link AliyunSmsAdapter}、{@link TencentSmsAdapter} 等「适配器 Adapter」）实现本接口，
 * 把统一的 {@link SmsRequest} 适配到对应 SDK 的差异化签名。</p>
 *
 * <p>调用方仅依赖本接口而无需感知任何具体服务商实现（依赖倒置）；新增服务商时
 * 只需新增一个实现本接口的适配器，无需修改调用方与既有适配器（开闭原则）。</p>
 *
 * @since 1.0.0
 */
public interface SmsSender {

    /**
     * 发送短信。
     *
     * @param req 统一短信发送请求，承载目标服务商、接收手机号、短信内容与签名等要素
     * @return 统一短信发送结果，包含服务商、回执标识、结果描述与完成时间等可观察信息
     */
    SmsSendResult send(SmsRequest req);

    /**
     * 返回当前适配器所对接的短信服务商标识。
     *
     * <p>该标识由实现类自身声明，供调用方建立「服务商标识 → 适配器」路由表，
     * 据以按 {@link SmsRequest#getVendor()} 选取对应适配器，
     * 因而须在所有适配器实现之间保持唯一，不依赖 Spring 的 bean 名称。</p>
     *
     * @return 服务商标识，如 {@code "aliyun"}、{@code "tencent"}
     */
    String vendor();
}
