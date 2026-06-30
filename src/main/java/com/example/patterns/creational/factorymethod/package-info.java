/**
 * 工厂方法（Factory Method）模式示例包。
 *
 * <p>业务场景：按支付渠道创建支付处理器。系统支持多种支付渠道（微信、支付宝等），
 * 由工厂依据调用方传入的渠道标识返回对应的支付处理器实现，未知渠道则返回错误且不创建实例
 * （对应需求 2.3、2.4、10.4、10.5）。</p>
 *
 * <p>角色与对应类：</p>
 * <ul>
 *     <li>抽象产品 Product：{@link com.example.patterns.creational.factorymethod.PaymentProcessor}</li>
 *     <li>具体产品 ConcreteProduct：{@link com.example.patterns.creational.factorymethod.WechatPaymentProcessor}、
 *         {@link com.example.patterns.creational.factorymethod.AlipayPaymentProcessor}</li>
 *     <li>工厂 Factory：{@link com.example.patterns.creational.factorymethod.PaymentProcessorFactory}</li>
 *     <li>领域数据对象：{@link com.example.patterns.creational.factorymethod.domain.PaymentContext}、
 *         {@link com.example.patterns.creational.factorymethod.domain.PayResult}</li>
 * </ul>
 *
 * <p>与 Spring 的结合：各具体产品以 {@code @Component} 交由容器管理，工厂以 {@code List} 注入
 * 全部实现并基于各实现声明的渠道标识构建路由表，新增渠道只需新增实现类即可自动接入（开闭原则）。</p>
 *
 * @since 1.0.0
 */
package com.example.patterns.creational.factorymethod;
