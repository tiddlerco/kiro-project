package com.example.patterns.structural.decorator.domain;

import lombok.Data;

/**
 * 通知内容。
 *
 * <p>承载一次通知发送所需的业务字段（接收人、标题、正文），作为
 * {@link com.example.patterns.structural.decorator.NotifySender#send} 的入参，在调用方、
 * 各装饰器与基础发送器之间传递通知要素。</p>
 *
 * <p>本类为纯数据对象（DTO），不承载任何发送业务逻辑；getter/setter 由 Lombok 的
 * {@link Data} 注解生成。装饰器在「委派前」增强内容时，应基于 {@link #copy()} 副本修改，
 * 以避免污染调用方传入的原始对象。</p>
 *
 * @since 1.0.0
 */
@Data
public class NotifyContent {

    /**
     * 接收人标识（如手机号、用户 ID 或邮箱）。
     */
    private String receiver;

    /**
     * 通知标题。
     */
    private String title;

    /**
     * 通知正文。
     */
    private String body;

    /**
     * 复制出一个字段值完全相同的新通知内容对象。
     *
     * <p>装饰器在「委派前」增强内容时基于副本修改而非直接改动调用方传入的原始对象，
     * 从而避免装饰链对外部内容产生副作用（无副作用是装饰能力可任意叠加组合的前提）。</p>
     *
     * @return 与当前对象各字段值相等的新通知内容副本
     */
    public NotifyContent copy() {
        NotifyContent copy = new NotifyContent();
        copy.setReceiver(this.receiver);
        copy.setTitle(this.title);
        copy.setBody(this.body);
        return copy;
    }
}
