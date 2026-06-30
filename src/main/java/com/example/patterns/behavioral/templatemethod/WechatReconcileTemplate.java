package com.example.patterns.behavioral.templatemethod;

import com.example.patterns.behavioral.templatemethod.domain.ReconcileRecord;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 微信支付对账模板。
 *
 * <p>模板方法模式中的「具体类（ConcreteClass）」角色之一，复用
 * {@link AbstractReconcileTemplate} 固定的对账骨架，仅实现因渠道而异的两个抽象步骤：
 * 拉取微信支付对账文件、按微信文件格式解析。微信对账文件以竖线分隔、首行为英文表头，
 * 与支付宝格式明显不同，恰好凸显「骨架不变、步骤各异」的模板方法价值。</p>
 *
 * <p>作为 Spring 组件交由容器管理，由上层依据渠道标识 {@value #CHANNEL_WECHAT} 路由获取。</p>
 *
 * @since 1.0.0
 */
@Component
public class WechatReconcileTemplate extends AbstractReconcileTemplate {

    /**
     * 微信支付渠道标识。
     */
    private static final String CHANNEL_WECHAT = "wechat";

    /**
     * 微信对账文件字段分隔符的正则表达式（竖线为正则元字符，需转义）。
     */
    private static final String FIELD_SEPARATOR_REGEX = "\\|";

    /**
     * 微信对账文件表头行。
     */
    private static final String HEADER_LINE = "order_no|amount";

    /**
     * 拉取微信支付对账文件。
     *
     * <p>模拟从微信支付商户平台下载当日对账文件，返回其原始文本行。文件以竖线分隔，
     * 首行为英文表头，其余为流水明细；本实现构造的流水与平台流水完全一致，以演示对平场景。</p>
     *
     * @return 微信支付对账文件的原始文本行集合
     */
    @Override
    protected List<String> fetch() {
        return Arrays.asList(
                HEADER_LINE,
                "PT20240001|100.00",
                "PT20240002|200.00",
                "PT20240003|300.00"
        );
    }

    /**
     * 解析微信支付对账文件。
     *
     * <p>按微信文件格式逐行解析：跳过空行与表头行，其余行以竖线切分为订单号与金额，
     * 转换为统一口径的对账记录。</p>
     *
     * @param rawLines 微信支付对账文件的原始文本行集合
     * @return 解析得到的微信侧对账记录列表
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
     * 返回微信支付渠道标识。
     *
     * @return 渠道标识 {@value #CHANNEL_WECHAT}
     */
    @Override
    public String channel() {
        return CHANNEL_WECHAT;
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
     * 将一行微信流水文本解析为对账记录。
     *
     * <p>以竖线切分订单号与金额字段，并去除首尾空白后构造对账记录。</p>
     *
     * @param line 一行微信流水文本，格式为「order_no|amount」
     * @return 解析得到的对账记录
     */
    private ReconcileRecord toRecord(String line) {
        String[] fields = line.split(FIELD_SEPARATOR_REGEX);
        String orderNo = fields[0].trim();
        BigDecimal amount = new BigDecimal(fields[1].trim());
        return ReconcileRecord.of(orderNo, amount);
    }
}
