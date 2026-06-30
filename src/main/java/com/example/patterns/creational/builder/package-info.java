/**
 * 建造者（Builder）模式示例包。
 *
 * <p>业务场景：通知消息的分步构建。一条通知由必选部件「接收人」与若干可选部件
 * 「标题 / 正文 / 附件 / 优先级」组成，不同业务所需的部件组合各异。建造者将「如何分步装配」
 * 与「装配完成后的产品」解耦：调用方经由统一入口链式设置所需部件，并在收尾时校验必选部件、
 * 产出不可变的完整通知消息（对应需求 2.6）。</p>
 *
 * <p>角色与对应类：</p>
 * <ul>
 *     <li>产品 Product：{@link com.example.patterns.creational.builder.NotificationMessage}
 *         （不可变，提供静态入口 {@code builder()}）</li>
 *     <li>具体建造者 Builder：{@link com.example.patterns.creational.builder.NotificationMessageBuilder}
 *         （链式 {@code to()/title()/content()/attach()/priority()}，{@code build()} 校验必选部件
 *         「接收人」，缺失则抛 {@link com.example.patterns.common.exception.ServiceException}）</li>
 *     <li>领域值对象：{@link com.example.patterns.creational.builder.domain.NotificationAttachment}（附件）、
 *         {@link com.example.patterns.creational.builder.domain.NotificationPriority}（优先级）</li>
 * </ul>
 *
 * <p>与 Spring 的结合：建造者为无状态的轻量构建工具，每次构建经 {@code NotificationMessage.builder()}
 * 新建实例，由演示服务调用其完成对象装配，构建逻辑与产品对象彼此解耦。</p>
 *
 * @since 1.0.0
 */
package com.example.patterns.creational.builder;
