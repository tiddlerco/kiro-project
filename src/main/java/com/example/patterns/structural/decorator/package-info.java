/**
 * 装饰器模式（Decorator）示例子包 —— 通知发送能力增强。
 *
 * <p>本子包以「通知发送」为业务场景演示装饰器模式：在不修改基础发送逻辑、也不通过继承
 * 派生子类的前提下，为通知发送动态叠加签名、加密、日志等增强能力，且各能力可按任意子集
 * 与任意顺序自由组合（需求 3.3、8.3）。</p>
 *
 * <p>角色—类映射：</p>
 * <ul>
 *     <li>抽象组件（Component）：{@link com.example.patterns.structural.decorator.NotifySender}</li>
 *     <li>具体组件（ConcreteComponent）：{@link com.example.patterns.structural.decorator.BaseNotifySender}</li>
 *     <li>抽象装饰器（Decorator）：{@link com.example.patterns.structural.decorator.NotifyDecorator}</li>
 *     <li>具体装饰器（ConcreteDecorator）：{@link com.example.patterns.structural.decorator.SignatureDecorator}（签名）、
 *         {@link com.example.patterns.structural.decorator.EncryptDecorator}（加密）、
 *         {@link com.example.patterns.structural.decorator.LogDecorator}（日志）</li>
 * </ul>
 *
 * <p>抽象装饰器以组合方式持有被装饰的 {@code NotifySender}（而非继承具体组件），体现
 * 「组合优于继承」；每个具体装饰器在委派前/后插入增强行为，装饰链由调用方/服务按需组装。</p>
 *
 * @since 1.0.0
 */
package com.example.patterns.structural.decorator;
