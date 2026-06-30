package com.example.patterns.behavioral.templatemethod;

import com.example.patterns.behavioral.templatemethod.domain.ReconcileRecord;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 支付宝对账模板。
 *
 * <p>模板方法模式中的「具体类（ConcreteClass）」角色之一，复用
 * {@link AbstractReconcileTemplate} 固定的对账骨架，仅实现因渠道而异的两个抽象步骤：
 * 拉取支付宝对账文件、按支付宝文件格式解析。支付宝对账文件以英文逗号分隔、首行为
 * 中文表头，本实现据此完成解析。</p>
 *
 * <p>作为 Spring 组件交由容器管理，由上层依据渠道标识 {@value #CHANNEL_ALIPAY} 路由获取。</p>
 *
 * @since 1.0.0
 */
@Component
public class AlipayReconcileTemplate extends AbstractReconcileTemplate {

    /**
     * 支付宝渠道标识。
     */
    private static final String CHANNEL_ALIPAY = "alipay";

    /**
     * 支付宝对账文件字段分隔符。
     */
    private static final String FIELD_SEPARATOR = ",";

    /**
     * 支付宝对账文件表头行。
     */
    private static final String HEADER_LINE = "订单号,金额";

    /**
     * 拉取支付宝对账文件。
     *
     * <p>模拟从支付宝开放平台下载当日对账文件，返回其原始文本行。文件以逗号分隔，
     * 首行为表头，其余为流水明细；其中刻意构造了金额不符与单边账数据，以演示差异检出。</p>
     *
     * @return 支付宝对账文件的原始文本行集合
     */
    @Override
    protected List<String> fetch() {
        return Arrays.asList(
                HEADER_LINE,
                "PT20240001,100.00",
                "PT20240002,250.00",
                "PT20240004,400.00"
        );
    }

    /**
     * 解析支付宝对账文件。
     *
     * <p>按支付宝文件格式逐行解析：跳过空行与表头行，其余行以逗号切分为订单号与金额，
     * 转换为统一口径的对账记录。</p>
     *
     * @param rawLines 支付宝对账文件的原始文本行集合
     * @return 解析得到的支付宝侧对账记录列表
     */
    @Override
    protected List<ReconcileRecord> parse(List<String> rawLines) {
        List<ReconcileRecord> records = new ArrayList<>();
        for (String line : rawLines) {
            if (isDataLine(line)) {
                records.add(toRecord(line));
            }
        }
        return records;
    }

    /**
     * 返回支付宝渠道标识。
     *
     * @return 渠道标识 {@value #CHANNEL_ALIPAY}
     */
    @Override
    public String channel() {
        return CHANNEL_ALIPAY;
    }

    /**
     * 判断某一行是否为可解析的流水数据行。
     *
     * <p>空行与表头行不参与解析，予以排除。</p>
     *
     * @param line 对账文件中的一行原始文本
     * @return 当该行为非空且非表头的流水数据行时返回 {@code true}，否则返回 {@code false}
     */
    private boolean isDataLine(String line) {
        return line != null && !line.trim().isEmpty() && !HEADER_LINE.equals(line.trim());
    }

    /**
     * 将一行支付宝流水文本解析为对账记录。
     *
     * <p>以逗号切分订单号与金额字段，并去除首尾空白后构造对账记录。</p>
     *
     * @param line 一行支付宝流水文本，格式为「订单号,金额」
     * @return 解析得到的对账记录
     */
    private ReconcileRecord toRecord(String line) {
        String[] fields = line.split(FIELD_SEPARATOR);
        String orderNo = fields[0].trim();
        BigDecimal amount = new BigDecimal(fields[1].trim());
        return ReconcileRecord.of(orderNo, amount);
    }
}
