package com.example.patterns.structural.decorator;

import com.example.patterns.structural.decorator.domain.NotifyCapability;
import com.example.patterns.structural.decorator.domain.NotifyContent;
import com.example.patterns.structural.decorator.domain.SendResult;

/**
 * 签名装饰器。
 *
 * <p>装饰器模式中的「具体装饰器（ConcreteDecorator）」角色之一：在委派发送之前，于通知正文
 * 末尾追加一段与内容绑定的签名信息；在委派返回之后，于结果中标记已应用「签名」能力。</p>
 *
 * <p>可与其他装饰器任意叠加组合，叠加后仍能在结果中体现签名能力（对应需求 3.3）。</p>
 *
 * @since 1.0.0
 */
public class SignatureDecorator extends NotifyDecorator {

    /**
     * 签名落款前缀，标识签发主体。
     */
    private static final String SIGNATURE_LABEL = "【签名】示例通知中心-";

    /**
     * 以被装饰的通知发送器构造签名装饰器。
     *
     * @param delegate 被装饰的通知发送器，作为本装饰器的委派目标
     */
    public SignatureDecorator(NotifySender delegate) {
        super(delegate);
    }

    /**
     * 发送通知并叠加「签名」能力。
     *
     * <p>委派前：基于内容副本在正文末尾追加签名串；委派后：在结果中标记「签名」能力。</p>
     *
     * @param content 通知内容
     * @return 叠加签名能力后的发送结果
     */
    @Override
    public SendResult send(NotifyContent content) {
        NotifyContent signed = content.copy();
        signed.setBody(appendSignature(content.getBody()));
        SendResult result = delegate.send(signed);
        result.addAppliedCapability(capability());
        return result;
    }

    /**
     * 在正文末尾追加与内容绑定的签名串。
     *
     * <p>签名值取自正文内容的散列值（十六进制），以体现「签名随内容变化」的特性。</p>
     *
     * @param body 原始正文内容
     * @return 追加签名后的正文内容
     */
    private String appendSignature(String body) {
        String safeBody = body == null ? "" : body;
        String signature = SIGNATURE_LABEL + Integer.toHexString(safeBody.hashCode());
        return safeBody + "\n--\n" + signature;
    }

    /**
     * 返回本装饰器代表的增强能力（签名）。
     *
     * @return 签名能力 {@link NotifyCapability#SIGNATURE}
     */
    @Override
    public NotifyCapability capability() {
        return NotifyCapability.SIGNATURE;
    }
}
