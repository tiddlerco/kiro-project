package com.example.patterns.structural.adapter.domain;

import lombok.Data;

/**
 * 统一短信发送请求。
 *
 * <p>适配器模式中作为「目标接口（Target）」{@link com.example.patterns.structural.adapter.SmsSender#send}
 * 的统一入参，向调用方屏蔽各第三方短信服务商（阿里云、腾讯云等）在 SDK 入参上的差异。
 * 调用方只需面向本统一请求编程，由各服务商对应的适配器负责将本请求的字段
 * 映射到目标 SDK 所需的差异化参数。</p>
 *
 * <p>本类为纯数据对象（DTO），不承载任何短信发送逻辑；getter/setter 由 Lombok
 * 的 {@link Data} 注解生成。</p>
 *
 * @since 1.0.0
 */
@Data
public class SmsRequest {

    /**
     * 目标短信服务商标识。
     *
     * <p>用于选取处理本次请求的适配器，如 {@code "aliyun"}、{@code "tencent"}，
     * 须与某个适配器自身声明的 {@link com.example.patterns.structural.adapter.SmsSender#vendor()} 一致。</p>
     */
    private String vendor;

    /**
     * 接收短信的手机号。
     */
    private String phone;

    /**
     * 短信正文内容。
     */
    private String content;

    /**
     * 短信签名。
     *
     * <p>部分服务商（如阿里云）要求显式携带短信签名；为可选字段，
     * 适配器在该字段为空时可采用各自的默认签名。</p>
     */
    private String signName;
}
