package com.example.patterns.structural.decorator.domain;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 通知发送结果。
 *
 * <p>承载装饰链执行通知发送后的可观察结果，包括是否成功、接收人、最终实际发送的正文，
 * 以及「已应用的增强能力列表」。其中 {@link #appliedCapabilities} 是体现装饰器叠加效果
 * 的关键：每个装饰器在委派返回后向其追加自身代表的能力，因而无论装饰能力以何种子集与
 * 顺序叠加，结果均能体现全部已叠加能力（对应需求 3.3）。</p>
 *
 * <p>本类为纯数据对象（DTO），getter/setter 由 Lombok 的 {@link Data} 注解生成；同时提供
 * {@link #success} 静态工厂方法构造基础成功结果，并提供 {@link #addAppliedCapability} 供
 * 装饰器追加能力标记。</p>
 *
 * @since 1.0.0
 */
@Data
public class SendResult {

    /**
     * 是否发送成功。
     */
    private boolean success;

    /**
     * 接收人标识。
     */
    private String receiver;

    /**
     * 最终实际发送的正文内容。
     *
     * <p>为经装饰链处理后的正文，可观察到签名追加、加密编码等增强效果。</p>
     */
    private String finalContent;

    /**
     * 已应用的增强能力列表。
     *
     * <p>按装饰链委派返回的先后顺序追加，用于体现本次发送实际叠加了哪些增强能力。</p>
     */
    private List<NotifyCapability> appliedCapabilities;

    /**
     * 结果描述信息。
     */
    private String message;

    /**
     * 发送完成时间。
     */
    private LocalDateTime sendTime;

    /**
     * 构建一个表示「基础发送成功」的结果，能力列表初始化为空、发送时间填充为当前时刻。
     *
     * <p>由具体组件 {@link com.example.patterns.structural.decorator.BaseNotifySender} 在完成
     * 基础发送后调用；后续各装饰器通过 {@link #addAppliedCapability} 在该结果上追加自身代表
     * 的增强能力。</p>
     *
     * @param receiver     接收人标识
     * @param finalContent 基础发送时的正文内容
     * @return 表示发送成功且已应用能力列表为空的发送结果
     */
    public static SendResult success(String receiver, String finalContent) {
        SendResult result = new SendResult();
        result.setSuccess(true);
        result.setReceiver(receiver);
        result.setFinalContent(finalContent);
        result.setAppliedCapabilities(new ArrayList<>());
        result.setMessage("通知发送成功");
        result.setSendTime(LocalDateTime.now());
        return result;
    }

    /**
     * 在结果中追加一项已应用的增强能力。
     *
     * <p>由各具体装饰器在「委派后」调用，用于在最终结果中留下可观察的能力痕迹，使得无论
     * 装饰能力以何种子集与顺序叠加，结果均能体现全部已叠加能力。</p>
     *
     * @param capability 本次叠加的增强能力
     * @return 当前发送结果自身，便于链式调用
     */
    public SendResult addAppliedCapability(NotifyCapability capability) {
        this.appliedCapabilities.add(capability);
        return this;
    }
}
