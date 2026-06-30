package com.example.patterns.structural.decorator.service;

import com.example.patterns.structural.decorator.BaseNotifySender;
import com.example.patterns.structural.decorator.EncryptDecorator;
import com.example.patterns.structural.decorator.LogDecorator;
import com.example.patterns.structural.decorator.NotifySender;
import com.example.patterns.structural.decorator.SignatureDecorator;
import com.example.patterns.structural.decorator.domain.NotifyCapability;
import com.example.patterns.structural.decorator.domain.NotifyContent;
import com.example.patterns.structural.decorator.domain.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * 装饰器通知组装服务。
 *
 * <p>装饰器模式的演示装配中心：以 Spring 容器注入的基础组件 {@link BaseNotifySender} 为装饰链
 * 最内层内核，依据调用方传入的增强能力列表，按顺序逐层包装对应的具体装饰器
 * （{@link SignatureDecorator} / {@link EncryptDecorator} / {@link LogDecorator}），再触发发送
 * 并返回可观察的 {@link SendResult}。</p>
 *
 * <p>装饰链的组装逻辑集中收敛于本服务，使 Controller 仅负责路由分发而不感知装饰器如何叠加，
 * 同时体现「组合优于继承」（对应需求 8.3）：各具体装饰器通过构造方法注入被装饰对象而非继承
 * 具体组件，从而支持任意子集、任意顺序的能力叠加。</p>
 *
 * @since 1.0.0
 */
@Service
public class DecoratorNotifyService {

    /**
     * 基础通知发送器，作为装饰链最内层的具体组件。
     *
     * <p>由 Spring 容器注入；各装饰器以其为内核按需层层包裹（协作角色由容器注入而非内部 new）。</p>
     */
    @Resource
    private BaseNotifySender baseNotifySender;

    /**
     * 依据指定的增强能力列表组装装饰链并发送通知。
     *
     * <p>以基础组件为内核，按 {@code capabilities} 的顺序逐层包装对应装饰器；能力列表为空时
     * 仅执行基础发送、不叠加任何增强能力。最终结果中的已应用能力列表可观察到本次实际叠加的
     * 全部增强能力。</p>
     *
     * @param content      通知内容，承载接收人、标题与正文
     * @param capabilities 需要叠加的增强能力列表，可为空（表示仅做基础发送）
     * @return 经装饰链处理后的发送结果，体现最终正文与全部已应用能力
     */
    public SendResult send(NotifyContent content, List<NotifyCapability> capabilities) {
        NotifySender sender = assembleChain(capabilities);
        return sender.send(content);
    }

    /**
     * 以基础组件为内核，按能力列表顺序逐层组装装饰链。
     *
     * <p>遍历能力列表，每遇到一项能力即用对应的具体装饰器包裹当前发送器，使后注入的能力成为
     * 装饰链的外层。能力列表为空时直接返回基础组件本身。</p>
     *
     * @param capabilities 需要叠加的增强能力列表，可为空
     * @return 组装完成的通知发送器（基础组件或被逐层包装后的装饰器）
     */
    private NotifySender assembleChain(List<NotifyCapability> capabilities) {
        NotifySender sender = baseNotifySender;
        if (CollectionUtils.isEmpty(capabilities)) {
            return sender;
        }
        for (NotifyCapability capability : capabilities) {
            sender = wrap(sender, capability);
        }
        return sender;
    }

    /**
     * 用指定增强能力对应的具体装饰器包裹被装饰的发送器。
     *
     * <p>依据能力枚举选择对应装饰器（签名 / 加密 / 日志）并通过构造方法注入被装饰对象，体现
     * 「组合优于继承」的扩展方式。</p>
     *
     * @param delegate   被装饰的通知发送器（作为新装饰器的委派目标）
     * @param capability 待叠加的增强能力，不允许为空
     * @return 包裹了指定增强能力后的通知发送器
     */
    private NotifySender wrap(NotifySender delegate, NotifyCapability capability) {
        switch (capability) {
            case SIGNATURE:
                return new SignatureDecorator(delegate);
            case ENCRYPT:
                return new EncryptDecorator(delegate);
            case LOG:
                return new LogDecorator(delegate);
            default:
                return delegate;
        }
    }
}
