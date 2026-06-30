package com.example.patterns.creational.builder.domain;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 构建通知消息请求。
 *
 * <p>建造者模式演示接口 {@code POST /pattern/builder/buildNotification} 的入参，承载构建一条
 * 通知所需的必选与可选部件：必选部件「接收人 {@link #receiver}」，可选部件「标题 {@link #title}」
 * 「正文 {@link #content}」「附件列表 {@link #attachments}」「优先级 {@link #priority}」。演示服务
 * 据此对象经 {@code NotificationMessage.builder()} 分步装配并构建出不可变的通知消息。</p>
 *
 * <p>本类为独立顶层请求对象（遵循「禁内部类」约束 C8），使用 Lombok {@code @Data} 生成
 * getter/setter/equals/hashCode/toString。必选部件「接收人」由 {@code @NotBlank} 做声明式校验，
 * 由 Controller 的 {@code @Validated} 触发；可选部件不施加非空约束，附件列表则以 {@code @Valid}
 * 级联校验其每个元素（C12）。</p>
 *
 * @since 1.0.0
 */
@Data
public class NotificationBuildRequest {

    /**
     * 接收人（必选部件）。
     *
     * <p>通知的目标接收方标识，如用户 ID、手机号或邮箱地址，不可为空白。</p>
     */
    @NotBlank(message = "接收人不能为空")
    private String receiver;

    /**
     * 通知标题（可选部件）。
     *
     * <p>为空时表示本条通知不含标题。</p>
     */
    private String title;

    /**
     * 通知正文（可选部件）。
     *
     * <p>为空时表示本条通知不含正文。</p>
     */
    private String content;

    /**
     * 通知附件列表（可选部件）。
     *
     * <p>为空或不传时表示本条通知不含附件；非空时其每个元素经 {@code @Valid} 级联校验。</p>
     */
    @Valid
    private List<NotificationAttachmentRequest> attachments;

    /**
     * 通知优先级（可选部件）。
     *
     * <p>以枚举接收，请求中传入对应的枚举名（如 {@code HIGH}）；为空时表示未指定优先级、
     * 由下游通道按默认策略处理。传入非法枚举名时由全局异常处理器统一转换为错误响应。</p>
     */
    private NotificationPriority priority;
}
