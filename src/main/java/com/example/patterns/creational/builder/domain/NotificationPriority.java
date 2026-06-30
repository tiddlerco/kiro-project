package com.example.patterns.creational.builder.domain;

/**
 * 通知优先级。
 *
 * <p>作为通知消息的可选部件之一，标识一条通知的重要程度，供下游推送通道据此
 * 决定推送时机、提醒强度与展示样式。它是建造者模式中由
 * {@code NotificationMessageBuilder#priority(NotificationPriority)} 设置的可选业务值对象，
 * 以枚举表达有限且稳定的取值集合，避免使用魔法字符串。</p>
 *
 * @since 1.0.0
 */
public enum NotificationPriority {

    /** 低优先级：一般通知，可延迟或聚合后推送。 */
    LOW("低"),

    /** 普通优先级：默认级别，按常规节奏推送。 */
    NORMAL("普通"),

    /** 高优先级：重要通知，需尽快触达接收人。 */
    HIGH("高"),

    /** 紧急优先级：需立即触达并以强提醒方式呈现。 */
    URGENT("紧急");

    /**
     * 优先级的中文展示名称。
     *
     * <p>用于在演示结果或日志中以可读文案呈现优先级，区别于用于程序判断的枚举常量名。</p>
     */
    private final String label;

    /**
     * 使用中文展示名称构造优先级枚举常量。
     *
     * @param label 优先级的中文展示名称
     */
    NotificationPriority(String label) {
        this.label = label;
    }

    /**
     * 获取优先级的中文展示名称。
     *
     * @return 优先级的中文展示名称
     */
    public String getLabel() {
        return label;
    }
}
