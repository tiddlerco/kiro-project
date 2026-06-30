package com.example.patterns.creational.builder;

import com.example.patterns.common.exception.ServiceException;
import com.example.patterns.creational.builder.domain.NotificationAttachment;
import com.example.patterns.creational.builder.domain.NotificationPriority;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 通知消息建造者（建造者模式中的具体建造者 Builder）。
 *
 * <p>负责将一条通知消息的装配过程拆解为一系列语义清晰的链式步骤，把「如何分步设置部件」
 * 的职责从产品 {@link NotificationMessage} 中分离出来：调用方按需调用 {@link #to(String)}
 * 设置必选部件，并任意组合 {@link #title(String)}、{@link #content(String)}、
 * {@link #attach(NotificationAttachment)}、{@link #priority(NotificationPriority)} 设置可选部件，
 * 最终调用 {@link #build()} 在通过必选部件校验后产出完整且不可变的通知消息（对应需求 2.6）。</p>
 *
 * <p>设计说明：</p>
 * <ul>
 *     <li>各设置方法均返回建造者自身，以支持流畅的链式调用。</li>
 *     <li>构造方法为包级私有，引导调用方统一经由 {@link NotificationMessage#builder()} 创建建造者，
 *         保持「单一构建入口」。</li>
 *     <li>建造者本身可变、用于临时承载装配中的部件；产品则在 {@code build()} 时一次性定型为不可变对象，
 *         二者职责分离。</li>
 * </ul>
 *
 * @since 1.0.0
 */
public class NotificationMessageBuilder {

    /**
     * 接收人（必选部件）。
     *
     * <p>装配中的临时值，{@link #build()} 时校验其非空。</p>
     */
    private String receiver;

    /**
     * 通知标题（可选部件）。
     */
    private String title;

    /**
     * 通知正文（可选部件）。
     */
    private String content;

    /**
     * 通知附件列表（可选部件）。
     *
     * <p>由 {@link #attach(NotificationAttachment)} 逐个追加，初始为空列表。</p>
     */
    private final List<NotificationAttachment> attachments = new ArrayList<>();

    /**
     * 通知优先级（可选部件）。
     */
    private NotificationPriority priority;

    /**
     * 包级私有构造方法，仅由 {@link NotificationMessage#builder()} 调用，
     * 以约束调用方统一经由静态入口创建建造者实例。
     */
    NotificationMessageBuilder() {
    }

    /**
     * 设置接收人（必选部件）。
     *
     * @param receiver 通知接收人标识（如用户 ID、手机号或邮箱地址）
     * @return 当前建造者自身，以支持链式调用
     */
    public NotificationMessageBuilder to(String receiver) {
        this.receiver = receiver;
        return this;
    }

    /**
     * 设置通知标题（可选部件）。
     *
     * @param title 通知标题
     * @return 当前建造者自身，以支持链式调用
     */
    public NotificationMessageBuilder title(String title) {
        this.title = title;
        return this;
    }

    /**
     * 设置通知正文（可选部件）。
     *
     * @param content 通知正文
     * @return 当前建造者自身，以支持链式调用
     */
    public NotificationMessageBuilder content(String content) {
        this.content = content;
        return this;
    }

    /**
     * 追加一个通知附件（可选部件）。
     *
     * <p>可多次调用以追加多个附件，追加顺序即最终产品中的附件顺序；传入 {@code null} 时将被忽略，
     * 以避免向附件列表写入空元素。</p>
     *
     * @param attachment 待追加的通知附件
     * @return 当前建造者自身，以支持链式调用
     */
    public NotificationMessageBuilder attach(NotificationAttachment attachment) {
        if (attachment != null) {
            this.attachments.add(attachment);
        }
        return this;
    }

    /**
     * 设置通知优先级（可选部件）。
     *
     * @param priority 通知优先级
     * @return 当前建造者自身，以支持链式调用
     */
    public NotificationMessageBuilder priority(NotificationPriority priority) {
        this.priority = priority;
        return this;
    }

    /**
     * 校验必选部件并构建完整的通知消息。
     *
     * <p>在装配收尾时校验必选部件「接收人」：当其缺失（为 {@code null} 或仅含空白字符）时，
     * 抛出 {@link ServiceException} 以拒绝构建非法对象，该异常最终由全局异常处理器统一转换为
     * 可观察的错误响应，从而保证「非静默失败」（满足需求 2.6 及相关约束）。校验通过后，
     * 以当前已设置的各部件产出不可变的 {@link NotificationMessage}。</p>
     *
     * @return 包含全部已设置部件的完整通知消息
     * @throws ServiceException 当必选部件「接收人」缺失时抛出
     */
    public NotificationMessage build() {
        if (!StringUtils.hasText(receiver)) {
            throw new ServiceException("通知消息构建失败：必选部件「接收人」不能为空");
        }
        return new NotificationMessage(receiver, title, content, attachments, priority);
    }
}
