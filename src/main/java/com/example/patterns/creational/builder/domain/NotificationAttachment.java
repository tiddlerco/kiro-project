package com.example.patterns.creational.builder.domain;

import lombok.Getter;

/**
 * 通知附件。
 *
 * <p>作为通知消息的可选部件，描述随通知一并下发的附件资源（如对账单、营销海报、
 * 电子凭证等）。一条通知可携带零到多个附件，由建造者的
 * {@code NotificationMessageBuilder#attach(NotificationAttachment)} 方法逐个追加。</p>
 *
 * <p>本类被设计为不可变值对象：全部字段经构造方法一次性赋值，仅通过 {@link Getter}
 * 暴露读取访问而不提供 setter，从而可被多条通知消息安全共享、不会被意外篡改。</p>
 *
 * @since 1.0.0
 */
@Getter
public class NotificationAttachment {

    /**
     * 附件文件名（含扩展名）。
     *
     * <p>如「对账单_202406.pdf」，用于在接收端展示与落盘命名。</p>
     */
    private final String fileName;

    /**
     * 附件的可访问地址。
     *
     * <p>通常为对象存储的下载链接或签名 URL，接收端据此拉取附件内容。</p>
     */
    private final String url;

    /**
     * 附件大小（单位：字节）。
     *
     * <p>用于在发送前做大小校验或在接收端展示附件体积。</p>
     */
    private final long sizeBytes;

    /**
     * 使用文件名、访问地址与大小构造通知附件。
     *
     * @param fileName  附件文件名（含扩展名）
     * @param url       附件的可访问地址
     * @param sizeBytes 附件大小（单位：字节）
     */
    public NotificationAttachment(String fileName, String url, long sizeBytes) {
        this.fileName = fileName;
        this.url = url;
        this.sizeBytes = sizeBytes;
    }

    /**
     * 以更具表达力的静态工厂方式构造通知附件。
     *
     * @param fileName  附件文件名（含扩展名）
     * @param url       附件的可访问地址
     * @param sizeBytes 附件大小（单位：字节）
     * @return 各字段填充完成的不可变通知附件值对象
     */
    public static NotificationAttachment of(String fileName, String url, long sizeBytes) {
        return new NotificationAttachment(fileName, url, sizeBytes);
    }
}
