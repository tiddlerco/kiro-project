/**
 * 适配器（Adapter）模式示例包。
 *
 * <p>业务场景：通过统一的短信发送接口，对接至少 2 家接口签名互不相同的第三方短信服务商
 * （阿里云短信、腾讯云短信），使调用方无需感知各服务商 SDK 的差异（需求 3.2）。</p>
 *
 * <p>角色与对应类：</p>
 * <ul>
 *     <li>目标接口（Target）：{@link com.example.patterns.structural.adapter.SmsSender}</li>
 *     <li>被适配者（Adaptee，模拟第三方 SDK，签名各异）：
 *         {@link com.example.patterns.structural.adapter.AliyunSmsClient}、
 *         {@link com.example.patterns.structural.adapter.TencentSmsClient}</li>
 *     <li>适配器（Adapter）：
 *         {@link com.example.patterns.structural.adapter.AliyunSmsAdapter}、
 *         {@link com.example.patterns.structural.adapter.TencentSmsAdapter}</li>
 *     <li>数据对象：{@link com.example.patterns.structural.adapter.domain.SmsRequest}、
 *         {@link com.example.patterns.structural.adapter.domain.SmsSendResult}</li>
 * </ul>
 *
 * <p>设计要点：各适配器以「对象组合」方式持有被适配者并经 Spring 容器注入，
 * 体现「组合优于继承」与「依赖经容器注入而非内部 new」；新增服务商仅需新增一个
 * 实现 {@code SmsSender} 的适配器，无需改动调用方与既有适配器（开闭原则）。</p>
 *
 * @since 1.0.0
 */
package com.example.patterns.structural.adapter;
