package com.example.patterns.structural.adapter.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 统一短信发送结果。
 *
 * <p>适配器模式中作为「目标接口（Target）」{@link com.example.patterns.structural.adapter.SmsSender#send}
 * 的统一返回值，将各第三方短信服务商在 SDK 回执上的差异（阿里云返回 BizId、
 * 腾讯云返回流水号等）归一化为统一的可观察结果，供调用方判断发送是否成功并展示回执。</p>
 *
 * <p>本类为纯数据对象（DTO），getter/setter 由 Lombok 的 {@link Data} 注解生成；
 * 同时提供 {@link #success} 静态工厂方法，便于各适配器以表达力更强的方式构造成功结果，
 * 避免逐字段手工装配。</p>
 *
 * @since 1.0.0
 */
@Data
public class SmsSendResult {

    /**
     * 是否发送成功。
     */
    private boolean success;

    /**
     * 实际处理本次发送的服务商标识。
     *
     * <p>取自实际处理本次请求的适配器所声明的服务商标识，如 {@code "aliyun"}、{@code "tencent"}。</p>
     */
    private String vendor;

    /**
     * 接收短信的手机号。
     */
    private String phone;

    /**
     * 服务商返回的消息回执标识。
     *
     * <p>由具体服务商生成的唯一回执编号（阿里云的 BizId、腾讯云的流水号等），用于回执查询与问题追溯。</p>
     */
    private String messageId;

    /**
     * 结果描述信息。
     */
    private String message;

    /**
     * 发送完成时间。
     */
    private LocalDateTime sendTime;

    /**
     * 构建一个表示「短信发送成功」的结果，并自动填充完成时间为当前时刻。
     *
     * @param vendor    实际处理本次发送的服务商标识
     * @param phone     接收短信的手机号
     * @param messageId 服务商返回的消息回执标识
     * @param message   结果描述信息
     * @return 表示发送成功且各字段填充完成的短信发送结果
     */
    public static SmsSendResult success(String vendor, String phone, String messageId, String message) {
        SmsSendResult result = new SmsSendResult();
        result.setSuccess(true);
        result.setVendor(vendor);
        result.setPhone(phone);
        result.setMessageId(messageId);
        result.setMessage(message);
        result.setSendTime(LocalDateTime.now());
        return result;
    }
}
