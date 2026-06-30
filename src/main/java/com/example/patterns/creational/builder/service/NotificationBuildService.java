package com.example.patterns.creational.builder.service;

import com.example.patterns.common.exception.ServiceException;
import com.example.patterns.creational.builder.NotificationMessage;
import com.example.patterns.creational.builder.NotificationMessageBuilder;
import com.example.patterns.creational.builder.domain.NotificationAttachment;
import com.example.patterns.creational.builder.domain.NotificationAttachmentRequest;
import com.example.patterns.creational.builder.domain.NotificationBuildRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 通知消息构建服务（建造者模式演示的构建编排角色）。
 *
 * <p>承载「如何依据请求对象分步装配通知消息」的编排逻辑，使 Controller 仅负责路由分发、不写
 * 任何构建细节（落实约束 C6）。本服务经统一入口 {@link NotificationMessage#builder()} 获取建造者，
 * 先设置必选部件「接收人」，再按需追加可选部件「标题 / 正文 / 附件 / 优先级」，最终调用
 * {@code build()} 产出不可变的通知消息；当必选部件缺失时，由建造者抛出 {@link ServiceException}，
 * 经全局异常处理器统一转换为可观察的错误响应（对应需求 2.6）。</p>
 *
 * @since 1.0.0
 */
@Service
public class NotificationBuildService {

    /**
     * 依据请求对象分步构建通知消息。
     *
     * <p>构建编排的统一入口：获取建造者后先设置必选部件「接收人」，再委派
     * {@link #applyOptionalParts(NotificationMessageBuilder, NotificationBuildRequest)} 追加可选部件，
     * 最终 {@code build()} 校验必选部件并产出不可变结果。必选部件缺失时建造者抛
     * {@link ServiceException}，由全局异常处理器统一处理。</p>
     *
     * @param request 构建通知消息请求，含必选「接收人」与可选「标题 / 正文 / 附件 / 优先级」
     * @return 依据请求装配完成的不可变通知消息
     */
    public NotificationMessage buildNotification(NotificationBuildRequest request) {
        NotificationMessageBuilder builder = NotificationMessage.builder()
                .to(request.getReceiver());
        applyOptionalParts(builder, request);
        return builder.build();
    }

    /**
     * 将请求中已提供的可选部件依次设置到建造者上。
     *
     * <p>仅对请求中实际提供的可选部件调用对应的建造者步骤：标题、正文、优先级按非空设置，
     * 附件列表委派 {@link #applyAttachments(NotificationMessageBuilder, List)} 逐个追加，从而保证
     * 未提供的部件在最终产品中维持「未设置」语义。</p>
     *
     * @param builder 正在装配通知消息的建造者
     * @param request 构建通知消息请求
     */
    private void applyOptionalParts(NotificationMessageBuilder builder, NotificationBuildRequest request) {
        builder.title(request.getTitle())
                .content(request.getContent())
                .priority(request.getPriority());
        applyAttachments(builder, request.getAttachments());
    }

    /**
     * 将请求中的附件子结构逐个追加到建造者上。
     *
     * <p>附件列表为空时直接返回、不追加任何附件；非空时将每个附件请求映射为领域值对象后，
     * 经建造者的 {@code attach(...)} 步骤逐个追加，追加顺序与请求列表顺序一致。</p>
     *
     * @param builder     正在装配通知消息的建造者
     * @param attachments 附件请求列表，允许为 {@code null} 或空
     */
    private void applyAttachments(NotificationMessageBuilder builder, List<NotificationAttachmentRequest> attachments) {
        if (CollectionUtils.isEmpty(attachments)) {
            return;
        }
        for (NotificationAttachmentRequest attachment : attachments) {
            builder.attach(toAttachment(attachment));
        }
    }

    /**
     * 将单个附件请求子对象映射为附件领域值对象。
     *
     * @param request 附件请求子对象，含文件名、访问地址与大小
     * @return 由请求字段填充的附件领域值对象；当请求为 {@code null} 时返回 {@code null} 交由建造者忽略
     */
    private NotificationAttachment toAttachment(NotificationAttachmentRequest request) {
        if (request == null) {
            return null;
        }
        return NotificationAttachment.of(
                request.getFileName(), request.getUrl(), request.getSizeBytes());
    }
}
