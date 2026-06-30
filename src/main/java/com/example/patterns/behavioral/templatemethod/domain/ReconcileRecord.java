package com.example.patterns.behavioral.templatemethod.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 对账记录。
 *
 * <p>承载一笔参与对账的交易流水，是对账比对的最小单元。平台侧流水与渠道侧
 * 对账文件解析后均以本对象表达，统一比对口径。其中「订单号」作为两侧流水
 * 配对的业务主键，「交易金额」为逐笔比对的核心字段。</p>
 *
 * <p>本类为纯数据对象（DTO），getter/setter 由 Lombok 的 {@link Data} 注解生成；
 * 同时提供 {@link #of} 静态工厂方法，便于以表达力更强的方式构造记录。</p>
 *
 * @since 1.0.0
 */
@Data
public class ReconcileRecord {

    /**
     * 订单号。
     *
     * <p>对账比对的业务主键，平台流水与渠道流水依据该字段两两配对。</p>
     */
    private String orderNo;

    /**
     * 交易金额（单位：元）。
     *
     * <p>逐笔对账的核心比对字段，两侧金额不一致即视为差异。</p>
     */
    private BigDecimal amount;

    /**
     * 交易时间。
     */
    private LocalDateTime tradeTime;

    /**
     * 构建一笔对账记录，并将交易时间默认填充为当前时刻。
     *
     * @param orderNo 订单号，作为对账比对的业务主键
     * @param amount  交易金额（单位：元）
     * @return 填充完成的对账记录
     */
    public static ReconcileRecord of(String orderNo, BigDecimal amount) {
        ReconcileRecord record = new ReconcileRecord();
        record.setOrderNo(orderNo);
        record.setAmount(amount);
        record.setTradeTime(LocalDateTime.now());
        return record;
    }
}
