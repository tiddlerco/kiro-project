package com.example.patterns.creational.builder.domain;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;

/**
 * 通知附件请求子对象。
 *
 * <p>作为建造者演示接口 {@code NotificationBuildRequest} 的附件子结构，描述随通知一并下发的
 * 单个附件资源。一条通知可携带零到多个附件，由请求对象以列表形式承载，再由演示服务逐个交给
 * 建造者的 {@code NotificationMessageBuilder#attach(...)} 步骤追加。</p>
 *
 * <p>本类为独立顶层请求对象（遵循「禁内部类」约束 C8），使用 Lombok {@code @Data} 生成
 * getter/setter/equals/hashCode/toString，并以 JSR-303 注解对字段做声明式校验（C12）。</p>
 *
 * @since 1.0.0
 */
@Data
public class NotificationAttachmentRequest {

    /**
     * 附件文件名（含扩展名）。
     *
     * <p>如「对账单_202406.pdf」，用于接收端展示与落盘命名，不可为空白。</p>
     */
    @NotBlank(message = "附件文件名不能为空")
    private String fileName;

    /**
     * 附件的可访问地址。
     *
     * <p>通常为对象存储的下载链接或签名 URL，接收端据此拉取附件内容，不可为空白。</p>
     */
    @NotBlank(message = "附件访问地址不能为空")
    private String url;

    /**
     * 附件大小（单位：字节）。
     *
     * <p>用于发送前的大小校验或接收端展示附件体积，取值须为非负数。</p>
     */
    @PositiveOrZero(message = "附件大小不能为负数")
    private long sizeBytes;
}
