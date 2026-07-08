package com.example.patterns.behavioral.chain;

import com.example.patterns.behavioral.chain.domain.RiskCheckResult;
import com.example.patterns.behavioral.chain.domain.RiskContext;
import com.example.patterns.behavioral.chain.handler.RiskRuleHandler;

import java.util.List;

/**
 * 记录调用轨迹的风控规则处理器探针（仅测试使用）。
 *
 * <p>用于属性测试验证责任链的「短路」不变式：本探针包装一个真实的
 * {@link RiskRuleHandler} 委托对象，在每次 {@link #handle(RiskContext)} 被调用时先向共享的
 * 执行轨迹列表登记自身标识，再原样返回委托对象的判定结果。如此既完整保留真实规则节点的
 * 拦截阈值语义，又使「某节点被拦截后其后节点是否仍被执行」这一链驱动行为可被机器观测。</p>
 *
 * <p>本类刻意提取为独立测试类而非内部类，以遵循「一类一文件、禁内部类」的工程约定，
 * 便于在多个属性用例中复用。</p>
 *
 * @since 1.0.0
 */
public class RecordingRiskRuleHandler implements RiskRuleHandler {

    /**
     * 被包装的真实规则节点，承担实际的拦截判定。
     */
    private final RiskRuleHandler delegate;

    /**
     * 本探针的标识，登记到执行轨迹中用于区分调用来源。
     */
    private final String tag;

    /**
     * 多个探针共享的执行轨迹列表，按调用先后顺序记录被执行节点的标识。
     */
    private final List<String> executionLog;

    /**
     * 构造一个包装真实节点并记录调用轨迹的探针。
     *
     * @param delegate     被包装的真实规则节点，其判定结果与排序序号被原样沿用
     * @param tag          本探针的标识，用于在执行轨迹中区分不同节点
     * @param executionLog 多个探针共享的执行轨迹列表，用于观测调用顺序与短路行为
     */
    public RecordingRiskRuleHandler(RiskRuleHandler delegate, String tag, List<String> executionLog) {
        this.delegate = delegate;
        this.tag = tag;
        this.executionLog = executionLog;
    }

    /**
     * 记录本节点被执行，随后返回委托节点的真实判定结果。
     *
     * <p>先将本探针标识追加到共享执行轨迹（证明本节点确被链驱动执行），再委托真实节点完成
     * 拦截判定并原样返回，从而不改变链的传递与短路语义。</p>
     *
     * @param ctx 沿链传递的风控校验上下文
     * @return 委托真实节点得出的校验结果
     */
    @Override
    public RiskCheckResult handle(RiskContext ctx) {
        executionLog.add(tag);
        return delegate.handle(ctx);
    }

    /**
     * 返回本节点的排序序号，直接沿用被包装真实节点的序号。
     *
     * @return 委托真实节点的排序序号
     */
    @Override
    public int order() {
        return delegate.order();
    }

    /**
     * 返回本探针的标识。
     *
     * @return 探针标识
     */
    public String getTag() {
        return tag;
    }
}
