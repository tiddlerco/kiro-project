package com.example.patterns.behavioral.templatemethod.domain;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 对账差异明细。
 *
 * <p>承载一笔对账差异的完整信息，是差异报告（{@link ReconcileReport}）的明细项。
 * 通过差异类型、订单号、两侧金额与文字描述，清晰刻画该笔流水「差在哪里」。
 * 对于单边账场景，缺失一侧的金额为 {@code null}。</p>
 *
 * <p>本类为纯数据对象（DTO），getter/setter 由 Lombok 的 {@link Data} 注解生成；
 * 同时按三类差异分别提供静态工厂方法，避免调用方逐字段手工装配并自动生成可读描述。</p>
 *
 * @since 1.0.0
 */
@Data
public class ReconcileDifference {

    /**
     * 订单号。
     */
    private String orderNo;

    /**
     * 差异类型。
     */
    private ReconcileDiffType type;

    /**
     * 平台侧交易金额（单位：元）。
     *
     * <p>当差异类型为「渠道有平台无」时，平台侧无此流水，该值为 {@code null}。</p>
     */
    private BigDecimal platformAmount;

    /**
     * 渠道侧交易金额（单位：元）。
     *
     * <p>当差异类型为「平台有渠道无」时，渠道侧无此流水，该值为 {@code null}。</p>
     */
    private BigDecimal channelAmount;

    /**
     * 差异的可读文字描述。
     */
    private String description;

    /**
     * 构建一笔「平台有渠道无」差异。
     *
     * @param orderNo        订单号
     * @param platformAmount 平台侧交易金额
     * @return 描述平台单边账的差异明细
     */
    public static ReconcileDifference platformOnly(String orderNo, BigDecimal platformAmount) {
        ReconcileDifference difference = new ReconcileDifference();
        difference.setOrderNo(orderNo);
        difference.setType(ReconcileDiffType.PLATFORM_ONLY);
        difference.setPlatformAmount(platformAmount);
        difference.setDescription(String.format("平台存在订单[%s]金额[%s]，渠道对账文件中缺失", orderNo, platformAmount));
        return difference;
    }

    /**
     * 构建一笔「渠道有平台无」差异。
     *
     * @param orderNo       订单号
     * @param channelAmount 渠道侧交易金额
     * @return 描述渠道单边账的差异明细
     */
    public static ReconcileDifference channelOnly(String orderNo, BigDecimal channelAmount) {
        ReconcileDifference difference = new ReconcileDifference();
        difference.setOrderNo(orderNo);
        difference.setType(ReconcileDiffType.CHANNEL_ONLY);
        difference.setChannelAmount(channelAmount);
        difference.setDescription(String.format("渠道存在订单[%s]金额[%s]，平台流水中缺失", orderNo, channelAmount));
        return difference;
    }

    /**
     * 构建一笔「金额不一致」差异。
     *
     * @param orderNo        订单号
     * @param platformAmount 平台侧交易金额
     * @param channelAmount  渠道侧交易金额
     * @return 描述两侧金额不符的差异明细
     */
    public static ReconcileDifference amountMismatch(String orderNo, BigDecimal platformAmount, BigDecimal channelAmount) {
        ReconcileDifference difference = new ReconcileDifference();
        difference.setOrderNo(orderNo);
        difference.setType(ReconcileDiffType.AMOUNT_MISMATCH);
        difference.setPlatformAmount(platformAmount);
        difference.setChannelAmount(channelAmount);
        difference.setDescription(String.format("订单[%s]平台金额[%s]与渠道金额[%s]不一致", orderNo, platformAmount, channelAmount));
        return difference;
    }
}
