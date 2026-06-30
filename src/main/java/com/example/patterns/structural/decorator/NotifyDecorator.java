package com.example.patterns.structural.decorator;

import com.example.patterns.structural.decorator.domain.NotifyCapability;

/**
 * 通知发送装饰器。
 *
 * <p>装饰器模式中的「抽象装饰器（Decorator）」角色：自身实现 {@link NotifySender}，同时以
 * 「组合」方式持有一个被装饰的 {@link NotifySender} 委派对象（而非通过继承具体组件来扩展），
 * 从而体现「组合优于继承」（对应需求 8.3），并支持各装饰器之间任意嵌套叠加。</p>
 *
 * <p>各具体装饰器在自身的 {@code send} 实现中，于「委派给被装饰对象之前/之后」插入增强行为
 * （如修改内容、记录日志、在结果中标记能力）。调用方可按需将多个装饰器层层包裹，组合出
 * 不同的增强能力链。</p>
 *
 * @since 1.0.0
 */
public abstract class NotifyDecorator implements NotifySender {

    /**
     * 被装饰的通知发送器（委派目标）。
     *
     * <p>以抽象的 {@link NotifySender} 类型声明而非具体实现类，使装饰器既可包裹基础组件，
     * 也可包裹其他装饰器，从而实现增强能力的任意叠加（依赖倒置）。</p>
     */
    protected final NotifySender delegate;

    /**
     * 以被装饰的通知发送器构造装饰器。
     *
     * @param delegate 被装饰的通知发送器，作为本装饰器的委派目标
     */
    protected NotifyDecorator(NotifySender delegate) {
        this.delegate = delegate;
    }

    /**
     * 返回当前装饰器所代表的增强能力。
     *
     * <p>由各具体装饰器声明，用于在发送结果中标记本层叠加的能力，也便于上层按能力标识
     * 动态组装装饰链。</p>
     *
     * @return 本装饰器代表的增强能力
     */
    public abstract NotifyCapability capability();
}
