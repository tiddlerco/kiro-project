package com.example.patterns.structural.decorator.domain;

import lombok.Getter;

/**
 * 通知增强能力。
 *
 * <p>枚举装饰器模式中各具体装饰器所代表的可叠加能力（签名、加密、日志）。每个具体装饰器
 * 声明其对应的能力，并在发送结果中标记该能力，从而使「最终结果体现全部已叠加能力」可被
 * 观察与校验（对应需求 3.3 与正确性属性「装饰器能力任意叠加」）。</p>
 *
 * <p>本枚举亦作为「能力标识 → 装饰器」的约定，便于上层按能力列表动态组装装饰链。
 * getter 由 Lombok 的 {@link Getter} 注解生成。</p>
 *
 * @since 1.0.0
 */
@Getter
public enum NotifyCapability {

    /**
     * 签名能力：在通知正文后追加与内容绑定的签名信息。
     */
    SIGNATURE("签名"),

    /**
     * 加密能力：对通知正文进行编码加密，避免明文传输。
     */
    ENCRYPT("加密"),

    /**
     * 日志能力：在发送前后记录可追溯的操作日志。
     */
    LOG("日志");

    /**
     * 能力的中文描述，便于在结果与日志中展示。
     */
    private final String description;

    /**
     * 构造通知增强能力枚举项。
     *
     * @param description 能力的中文描述
     */
    NotifyCapability(String description) {
        this.description = description;
    }
}
