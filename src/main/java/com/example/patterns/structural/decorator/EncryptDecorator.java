package com.example.patterns.structural.decorator;

import com.example.patterns.structural.decorator.domain.NotifyCapability;
import com.example.patterns.structural.decorator.domain.NotifyContent;
import com.example.patterns.structural.decorator.domain.SendResult;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 加密装饰器。
 *
 * <p>装饰器模式中的「具体装饰器（ConcreteDecorator）」角色之一：在委派发送之前，对通知正文
 * 进行编码加密；在委派返回之后，于结果中标记已应用「加密」能力。</p>
 *
 * <p>可与其他装饰器任意叠加组合，叠加后仍能在结果中体现加密能力（对应需求 3.3）。</p>
 *
 * @since 1.0.0
 */
public class EncryptDecorator extends NotifyDecorator {

    /**
     * 以被装饰的通知发送器构造加密装饰器。
     *
     * @param delegate 被装饰的通知发送器，作为本装饰器的委派目标
     */
    public EncryptDecorator(NotifySender delegate) {
        super(delegate);
    }

    /**
     * 发送通知并叠加「加密」能力。
     *
     * <p>委派前：基于内容副本对正文进行加密编码；委派后：在结果中标记「加密」能力。</p>
     *
     * @param content 通知内容
     * @return 叠加加密能力后的发送结果
     */
    @Override
    public SendResult send(NotifyContent content) {
        NotifyContent encrypted = content.copy();
        encrypted.setBody(encrypt(content.getBody()));
        SendResult result = delegate.send(encrypted);
        result.addAppliedCapability(capability());
        return result;
    }

    /**
     * 对正文进行编码加密。
     *
     * <p>此处以 Base64 编码模拟加密以便教学演示；真实场景应替换为 AES 等具备保密强度的
     * 加密算法。</p>
     *
     * @param body 原始正文内容
     * @return 加密编码后的正文内容
     */
    private String encrypt(String body) {
        String safeBody = body == null ? "" : body;
        return Base64.getEncoder().encodeToString(safeBody.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 返回本装饰器代表的增强能力（加密）。
     *
     * @return 加密能力 {@link NotifyCapability#ENCRYPT}
     */
    @Override
    public NotifyCapability capability() {
        return NotifyCapability.ENCRYPT;
    }
}
