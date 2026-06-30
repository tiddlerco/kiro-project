package com.example.patterns.structural.decorator;

import com.example.patterns.structural.decorator.domain.NotifyContent;
import com.example.patterns.structural.decorator.domain.SendResult;

/**
 * 通知发送器。
 *
 * <p>装饰器模式中的「抽象组件（Component）」角色，抽象出「发送通知」这一统一能力。
 * 具体组件 {@link BaseNotifySender} 承担真实的基础发送逻辑，抽象装饰器
 * {@link NotifyDecorator} 及其各具体装饰器在此能力之上叠加签名、加密、日志等增强行为。</p>
 *
 * <p>由于装饰器自身亦实现本接口，调用方与各装饰器均仅依赖本抽象类型而非具体实现类
 * （依赖倒置），从而支持将多个装饰器层层包裹、任意组合（对应需求 3.3）。</p>
 *
 * @since 1.0.0
 */
public interface NotifySender {

    /**
     * 发送一条通知。
     *
     * @param content 通知内容，承载接收人、标题与正文
     * @return 发送结果，包含是否成功、最终发送正文与已应用的增强能力等可观察信息
     */
    SendResult send(NotifyContent content);
}
