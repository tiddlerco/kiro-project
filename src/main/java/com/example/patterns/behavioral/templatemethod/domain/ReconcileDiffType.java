package com.example.patterns.behavioral.templatemethod.domain;

import lombok.Getter;

/**
 * 对账差异类型。
 *
 * <p>枚举平台流水与渠道对账文件逐笔比对时可能出现的三类差异，用于在差异报告中
 * 标注每一笔差异的性质，便于运营人员据此定位单边账或金额不符问题。</p>
 *
 * @since 1.0.0
 */
@Getter
public enum ReconcileDiffType {

    /**
     * 平台有、渠道无：平台侧存在该笔流水，但渠道对账文件中缺失（疑似渠道漏单）。
     */
    PLATFORM_ONLY("平台有渠道无"),

    /**
     * 渠道有、平台无：渠道对账文件存在该笔流水，但平台侧缺失（疑似平台漏记）。
     */
    CHANNEL_ONLY("渠道有平台无"),

    /**
     * 金额不一致：两侧均存在该笔流水，但交易金额不相等。
     */
    AMOUNT_MISMATCH("金额不一致");

    /**
     * 差异类型的中文描述。
     */
    private final String description;

    /**
     * 构造对账差异类型。
     *
     * @param description 差异类型的中文描述
     */
    ReconcileDiffType(String description) {
        this.description = description;
    }
}
