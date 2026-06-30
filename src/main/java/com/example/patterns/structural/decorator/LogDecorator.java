package com.example.patterns.structural.decorator;

import com.example.patterns.structural.decorator.domain.NotifyCapability;
import com.example.patterns.structural.decorator.domain.NotifyContent;
import com.example.patterns.structural.decorator.domain.SendResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 日志装饰器。
 *
 * <p>装饰器模式中的「具体装饰器（ConcreteDecorator）」角色之一：在委派发送之前记录「准备发送」
 * 日志，在委派返回之后记录「发送完成」日志，并于结果中标记已应用「日志」能力。本装饰器不修改
 * 通知内容，仅为发送过程附加可观察的操作轨迹。</p>
 *
 * <p>可与其他装饰器任意叠加组合，叠加后仍能在结果中体现日志能力（对应需求 3.3）。</p>
 *
 * @since 1.0.0
 */
public class LogDecorator extends NotifyDecorator {

    /**
     * 日志记录器，输出发送前后的可观察轨迹。
     */
    private static final Logger log = LoggerFactory.getLogger(LogDecorator.class);

    /**
     * 以被装饰的通知发送器构造日志装饰器。
     *
     * @param delegate 被装饰的通知发送器，作为本装饰器的委派目标
     */
    public LogDecorator(NotifySender delegate) {
        super(delegate);
    }

    /**
     * 发送通知并叠加「日志」能力。
     *
     * <p>委派前：记录准备发送的日志；委派后：记录发送结果日志并在结果中标记「日志」能力。</p>
     *
     * @param content 通知内容
     * @return 叠加日志能力后的发送结果
     */
    @Override
    public SendResult send(NotifyContent content) {
        log.info("准备发送通知，接收人：{}，标题：{}", content.getReceiver(), content.getTitle());
        SendResult result = delegate.send(content);
        log.info("通知发送完成，接收人：{}，是否成功：{}", result.getReceiver(), result.isSuccess());
        result.addAppliedCapability(capability());
        return result;
    }

    /**
     * 返回本装饰器代表的增强能力（日志）。
     *
     * @return 日志能力 {@link NotifyCapability#LOG}
     */
    @Override
    public NotifyCapability capability() {
        return NotifyCapability.LOG;
    }
}
