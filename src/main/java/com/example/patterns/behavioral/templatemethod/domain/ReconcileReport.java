package com.example.patterns.behavioral.templatemethod.domain;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 对账报告。
 *
 * <p>对账流程（{@code reconcile()}）的最终产出，汇总某一渠道本次对账的整体结论：
 * 包含参与对账的平台与渠道记录数、对平笔数、逐笔差异明细，以及是否对平、
 * 结论摘要与对账完成时间。是模板方法骨架中「比对」与「生成差异报告」两步的
 * 共同承载对象，供调用方据此判断账务是否平衡并展示差异。</p>
 *
 * <p>本类为纯数据对象（DTO），getter/setter 由 Lombok 的 {@link Data} 注解生成；
 * 另提供差异追加与差异计数等表达力方法，封装内部列表的读写细节。</p>
 *
 * @since 1.0.0
 */
@Data
public class ReconcileReport {

    /**
     * 对账渠道标识，如 {@code "alipay"}、{@code "wechat"}。
     */
    private String channel;

    /**
     * 平台侧参与对账的记录总数。
     */
    private int platformCount;

    /**
     * 渠道侧参与对账的记录总数。
     */
    private int channelCount;

    /**
     * 两侧成功对平（订单号配对且金额一致）的笔数。
     */
    private int matchedCount;

    /**
     * 差异明细列表。
     */
    private List<ReconcileDifference> differences = new ArrayList<>();

    /**
     * 本次对账是否完全对平（无任何差异）。
     */
    private boolean balanced;

    /**
     * 对账结论摘要文字。
     */
    private String summary;

    /**
     * 对账完成时间。
     */
    private LocalDateTime reconcileTime;

    /**
     * 向报告追加一笔差异明细。
     *
     * @param difference 待追加的差异明细
     */
    public void addDifference(ReconcileDifference difference) {
        this.differences.add(difference);
    }

    /**
     * 返回本次对账发现的差异总笔数。
     *
     * @return 差异明细的数量
     */
    public int getDiffCount() {
        return this.differences.size();
    }
}
