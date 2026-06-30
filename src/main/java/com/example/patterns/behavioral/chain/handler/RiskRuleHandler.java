package com.example.patterns.behavioral.chain.handler;

import com.example.patterns.behavioral.chain.domain.RiskCheckResult;
import com.example.patterns.behavioral.chain.domain.RiskContext;

/**
 * 风控规则处理器。
 *
 * <p>责任链模式中的「处理器（Handler）」抽象角色，抽象出「对风控上下文执行一条规则校验」
 * 这一可独立增减、可灵活排序的处理节点。黑名单、金额上限、频次等具体规则以独立实现类承担
 * 「具体处理器（ConcreteHandler）」角色，由链装配与驱动服务
 * {@link com.example.patterns.behavioral.chain.service.RiskRuleChain}
 * 按 {@link #order()} 排序组链并驱动请求沿链依次传递，调用方仅依赖本接口而无需感知具体
 * 节点实现及其顺序（依赖倒置、开闭原则，对应需求 4.4）。</p>
 *
 * <p>本接口刻意只暴露「校验」与「排序」两项职责，将「短路传递」的链路控制权收敛到链驱动服务，
 * 使各节点保持无状态、互不引用的纯规则判定，可单独测试、可任意重排。所有实现均须保证
 * {@link #handle} 返回非空结果（放行返回通过结果，拦截返回带命中节点与原因的拦截结果）。</p>
 *
 * @since 1.0.0
 */
public interface RiskRuleHandler {

    /**
     * 对风控上下文执行本节点的规则校验。
     *
     * <p>本方法只做单一规则的判定，不负责调用后续节点：放行时返回「通过」结果交由链继续传递，
     * 拦截时返回携带命中节点与拦截原因的「拦截」结果，由链据此短路。实现须保证返回值非空。</p>
     *
     * @param ctx 沿链传递的风控校验上下文
     * @return 本节点的校验结果，放行为通过结果、拦截为带命中节点与原因的拦截结果
     */
    RiskCheckResult handle(RiskContext ctx);

    /**
     * 返回本节点在责任链中的排序序号。
     *
     * <p>链驱动服务依据该序号对全部节点升序组链，数值越小越靠前执行。各实现须返回互不相同的
     * 序号以保证链路顺序确定。</p>
     *
     * @return 排序序号，数值越小越靠前执行
     */
    int order();
}
