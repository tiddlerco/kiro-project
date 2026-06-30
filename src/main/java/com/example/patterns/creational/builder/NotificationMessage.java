package com.example.patterns.creational.builder;

import com.example.patterns.creational.builder.domain.NotificationAttachment;
import com.example.patterns.creational.builder.domain.NotificationPriority;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 通知消息（建造者模式中的产品 Product）。
 *
 * <p>业务场景：一条对外发送的通知由若干部件组成——必选部件「接收人 {@link #receiver}」，
 * 以及可选部件「标题 {@link #title}」「正文 {@link #content}」「附件 {@link #attachments}」
 * 「优先级 {@link #priority}」。不同业务（验证码、对账提醒、营销推送等）所需部件组合各异，
 * 若以重叠构造方法（telescoping constructor）或对外暴露 setter 的方式装配，将导致构造方法爆炸
 * 或对象在装配过程中处于不一致的中间态。本类将「如何分步装配」的职责剥离至
 * {@link NotificationMessageBuilder}，自身仅作为装配完成后的不可变结果存在（对应需求 2.6）。</p>
 *
 * <p>不可变性设计：</p>
 * <ul>
 *     <li>全部字段声明为 {@code final}，仅通过 {@link Getter} 暴露读取访问、不提供任何 setter，
 *         避免对外暴露可变入口而破坏不可变性。</li>
 *     <li>构造方法为包级私有，仅供同包的 {@link NotificationMessageBuilder#build()} 在完成
 *         必选部件校验后调用；外部无法直接 {@code new}，只能经由静态入口 {@link #builder()} 构建，
 *         从而保证「产品只能通过建造者产出」。</li>
 *     <li>附件列表在构造时做防御性拷贝并包装为不可变视图，杜绝构建后经由外部引用旁路篡改。</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Getter
public class NotificationMessage {

    /**
     * 接收人（必选部件）。
     *
     * <p>通知的目标接收方标识，如用户 ID、手机号或邮箱地址。该部件缺失时建造者将拒绝构建，
     * 因此构建成功的通知消息其接收人必非空。</p>
     */
    private final String receiver;

    /**
     * 通知标题（可选部件）。
     *
     * <p>未设置时为 {@code null}，表示本条通知不含标题。</p>
     */
    private final String title;

    /**
     * 通知正文（可选部件）。
     *
     * <p>未设置时为 {@code null}，表示本条通知不含正文。</p>
     */
    private final String content;

    /**
     * 通知附件列表（可选部件）。
     *
     * <p>为不可变列表，元素顺序与建造者中逐个追加的顺序一致；未设置任何附件时为空列表
     * （而非 {@code null}），以避免调用方遍历时的空指针判断。</p>
     */
    private final List<NotificationAttachment> attachments;

    /**
     * 通知优先级（可选部件）。
     *
     * <p>未设置时为 {@code null}，表示本条通知未指定优先级、由下游通道按默认策略处理。</p>
     */
    private final NotificationPriority priority;

    /**
     * 包级私有全参构造方法，仅供 {@link NotificationMessageBuilder#build()} 在完成必选部件
     * 校验后调用，以保证「产品只能经由建造者构建」与对象的不可变性。
     *
     * <p>对传入的附件列表执行防御性拷贝并包装为不可变视图：当其为 {@code null} 或空时统一规整为
     * 空列表，否则复制其元素后包装为只读列表，从而隔离外部对内部状态的后续修改。</p>
     *
     * @param receiver    接收人（必选部件），由建造者保证非空后传入
     * @param title       通知标题（可选部件），允许为 {@code null}
     * @param content     通知正文（可选部件），允许为 {@code null}
     * @param attachments 通知附件列表（可选部件），允许为 {@code null} 或空
     * @param priority    通知优先级（可选部件），允许为 {@code null}
     */
    NotificationMessage(String receiver, String title, String content,
                        List<NotificationAttachment> attachments, NotificationPriority priority) {
        this.receiver = receiver;
        this.title = title;
        this.content = content;
        this.attachments = (attachments == null || attachments.isEmpty())
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(attachments));
        this.priority = priority;
    }

    /**
     * 获取构建通知消息的建造者入口。
     *
     * <p>建造者模式的统一构建入口：调用方经由本方法获取一个全新的
     * {@link NotificationMessageBuilder}，链式设置必选与可选部件后调用其
     * {@code build()} 得到完整且不可变的通知消息实例。</p>
     *
     * @return 一个尚未设置任何部件的全新通知消息建造者
     */
    public static NotificationMessageBuilder builder() {
        return new NotificationMessageBuilder();
    }
}
