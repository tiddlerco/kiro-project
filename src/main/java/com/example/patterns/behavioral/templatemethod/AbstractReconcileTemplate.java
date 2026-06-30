package com.example.patterns.behavioral.templatemethod;

import com.example.patterns.behavioral.templatemethod.domain.ReconcileDifference;
import com.example.patterns.behavioral.templatemethod.domain.ReconcileRecord;
import com.example.patterns.behavioral.templatemethod.domain.ReconcileReport;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 对账流程抽象模板。
 *
 * <p>模板方法模式中的「抽象类（AbstractClass）」角色，封装企业对账业务的算法骨架。
 * 对账的整体编排是稳定不变的：先拉取渠道对账文件、再解析为标准流水、然后与平台
 * 流水逐笔比对、最后生成差异报告；而「文件如何拉取」「文件如何解析」则因渠道
 * （支付宝、微信等）而异。本模板将稳定的编排固定在 {@link #reconcile()} 中，
 * 将易变的步骤下放为抽象方法交由具体渠道子类实现，从而复用骨架、隔离差异。</p>
 *
 * <p>各步骤的角色划分如下：</p>
 * <ul>
 *     <li>{@link #fetch()}：抽象步骤，拉取渠道侧对账文件原始内容，由子类实现。</li>
 *     <li>{@link #parse(List)}：抽象步骤，将原始内容解析为标准对账记录，由子类实现。</li>
 *     <li>{@link #compare(List)}：骨架内通用步骤，与平台流水逐笔比对得出差异报告。</li>
 *     <li>{@link #generateReport(ReconcileReport)}：骨架内通用步骤，补全报告结论摘要。</li>
 *     <li>{@link #loadPlatformRecords()}：钩子方法，提供平台侧流水，子类可按渠道重写。</li>
 * </ul>
 *
 * <p>{@link #reconcile()} 以 {@code final} 修饰，旨在固定算法骨架与步骤先后顺序，
 * 防止子类破坏「拉取→解析→比对→生成报告」这一不可变的对账流程。</p>
 *
 * @since 1.0.0
 */
public abstract class AbstractReconcileTemplate {

    /**
     * 对账模板方法（算法骨架）。
     *
     * <p>以固定顺序编排对账全流程：拉取对账文件 → 解析为标准流水 → 与平台流水比对
     * → 生成差异报告。本方法以 {@code final} 修饰，子类不可重写或调整步骤顺序，
     * 仅能通过实现其中的抽象步骤定制各渠道差异，以此保证对账骨架的稳定一致。</p>
     *
     * @return 本次对账的差异报告，含对平笔数、差异明细与对账结论
     */
    public final ReconcileReport reconcile() {
        // 步骤一：拉取渠道侧对账文件原始内容（抽象步骤，由具体渠道实现）
        List<String> rawFile = fetch();
        // 步骤二：将原始内容解析为标准对账记录（抽象步骤，由具体渠道实现）
        List<ReconcileRecord> channelRecords = parse(rawFile);
        // 步骤三：与平台流水逐笔比对，得出差异报告（骨架内通用步骤）
        ReconcileReport report = compare(channelRecords);
        // 步骤四：补全报告结论摘要，生成最终差异报告（骨架内通用步骤）
        return generateReport(report);
    }

    /**
     * 拉取渠道侧对账文件（抽象步骤）。
     *
     * <p>模拟从对应支付渠道下载对账文件，返回文件的原始文本行。不同渠道的文件
     * 来源、编码与格式各异，故交由具体子类实现。</p>
     *
     * @return 对账文件的原始文本行集合，每个元素代表文件中的一行
     */
    protected abstract List<String> fetch();

    /**
     * 解析渠道对账文件（抽象步骤）。
     *
     * <p>将 {@link #fetch()} 拉取到的原始文本行解析为统一口径的对账记录。不同渠道
     * 的文件格式（分隔符、字段顺序、表头等）互不相同，故交由具体子类实现。</p>
     *
     * @param rawLines 对账文件的原始文本行集合
     * @return 解析得到的渠道侧对账记录列表
     */
    protected abstract List<ReconcileRecord> parse(List<String> rawLines);

    /**
     * 返回当前对账模板所属的渠道标识。
     *
     * <p>由具体子类自报渠道标识，供上层依据标识路由到对应渠道的对账模板，
     * 在所有渠道实现之间须保持唯一，不依赖 Spring 的 bean 名称。</p>
     *
     * @return 渠道标识，如 {@code "alipay"}、{@code "wechat"}
     */
    public abstract String channel();

    /**
     * 加载平台侧对账流水（钩子方法）。
     *
     * <p>提供参与本次对账的平台侧交易流水，默认返回一份通用的模拟平台流水。
     * 子类可在确有需要时重写本方法，以贴合特定渠道对应的平台账务口径；
     * 不重写则沿用默认实现，体现模板方法中「可选扩展点」的设计意图。</p>
     *
     * @return 平台侧对账记录列表
     */
    protected List<ReconcileRecord> loadPlatformRecords() {
        List<ReconcileRecord> platformRecords = new ArrayList<>();
        platformRecords.add(ReconcileRecord.of("PT20240001", new BigDecimal("100.00")));
        platformRecords.add(ReconcileRecord.of("PT20240002", new BigDecimal("200.00")));
        platformRecords.add(ReconcileRecord.of("PT20240003", new BigDecimal("300.00")));
        return platformRecords;
    }

    /**
     * 比对平台流水与渠道流水（骨架内通用步骤）。
     *
     * <p>先经 {@link #loadPlatformRecords()} 取得平台侧流水，再与传入的渠道侧流水
     * 逐笔比对，得出对平笔数与各类差异明细。比对规则对所有渠道一致，故在抽象类中
     * 统一实现。</p>
     *
     * @param channelRecords 由 {@link #parse(List)} 解析得到的渠道侧对账记录
     * @return 已填充记录数、对平笔数与差异明细、但尚未补全结论摘要的对账报告
     */
    protected ReconcileReport compare(List<ReconcileRecord> channelRecords) {
        List<ReconcileRecord> platformRecords = loadPlatformRecords();
        return doCompare(platformRecords, channelRecords);
    }

    /**
     * 生成差异报告（骨架内通用步骤）。
     *
     * <p>在比对结果基础上补全对账结论：标记是否对平、生成结论摘要并记录对账完成
     * 时间。该收尾逻辑对所有渠道一致，故在抽象类中统一实现。</p>
     *
     * @param report 已完成比对、待补全结论的对账报告
     * @return 补全结论摘要后的最终差异报告
     */
    protected ReconcileReport generateReport(ReconcileReport report) {
        report.setReconcileTime(LocalDateTime.now());
        boolean balanced = report.getDiffCount() == 0;
        report.setBalanced(balanced);
        if (balanced) {
            report.setSummary(String.format("渠道[%s]对账完成，共核对%d笔，全部对平",
                    channel(), report.getMatchedCount()));
        } else {
            report.setSummary(String.format("渠道[%s]对账完成，对平%d笔，发现%d笔差异",
                    channel(), report.getMatchedCount(), report.getDiffCount()));
        }
        return report;
    }

    /**
     * 执行平台流水与渠道流水的逐笔比对。
     *
     * <p>以订单号为配对主键：两侧均存在且金额相等记为对平；金额不等记为「金额不一致」
     * 差异；仅一侧存在则记为对应的单边账差异。金额比较使用 {@link BigDecimal#compareTo}
     * 以按数值而非标度判定相等。</p>
     *
     * @param platformRecords 平台侧对账记录
     * @param channelRecords  渠道侧对账记录
     * @return 填充了记录数、对平笔数与差异明细的对账报告
     */
    private ReconcileReport doCompare(List<ReconcileRecord> platformRecords, List<ReconcileRecord> channelRecords) {
        ReconcileReport report = new ReconcileReport();
        report.setChannel(channel());
        report.setPlatformCount(platformRecords.size());
        report.setChannelCount(channelRecords.size());

        Map<String, ReconcileRecord> platformMap = toRecordMap(platformRecords);
        Map<String, ReconcileRecord> channelMap = toRecordMap(channelRecords);

        Set<String> allOrderNos = new LinkedHashSet<>();
        allOrderNos.addAll(platformMap.keySet());
        allOrderNos.addAll(channelMap.keySet());

        int matchedCount = 0;
        for (String orderNo : allOrderNos) {
            ReconcileRecord platformRecord = platformMap.get(orderNo);
            ReconcileRecord channelRecord = channelMap.get(orderNo);
            if (platformRecord != null && channelRecord != null) {
                if (platformRecord.getAmount().compareTo(channelRecord.getAmount()) == 0) {
                    matchedCount++;
                } else {
                    report.addDifference(ReconcileDifference.amountMismatch(
                            orderNo, platformRecord.getAmount(), channelRecord.getAmount()));
                }
            } else if (platformRecord != null) {
                report.addDifference(ReconcileDifference.platformOnly(orderNo, platformRecord.getAmount()));
            } else {
                report.addDifference(ReconcileDifference.channelOnly(orderNo, channelRecord.getAmount()));
            }
        }
        report.setMatchedCount(matchedCount);
        return report;
    }

    /**
     * 将对账记录列表转换为「订单号 → 记录」的映射。
     *
     * <p>以订单号为键建立索引，便于按订单号 O(1) 配对两侧流水；保持插入顺序以使
     * 后续差异明细的排列稳定可预期。</p>
     *
     * @param records 对账记录列表
     * @return 以订单号为键、对账记录为值的有序映射
     */
    private Map<String, ReconcileRecord> toRecordMap(List<ReconcileRecord> records) {
        Map<String, ReconcileRecord> recordMap = new LinkedHashMap<>();
        for (ReconcileRecord record : records) {
            recordMap.put(record.getOrderNo(), record);
        }
        return recordMap;
    }
}
