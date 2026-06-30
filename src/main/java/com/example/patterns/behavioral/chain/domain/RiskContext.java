package com.example.patterns.behavioral.chain.domain;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 风控校验上下文。
 *
 * <p>责任链模式中沿链传递的「请求对象」，承载一次风控规则校验所需的全部输入：发起交易的
 * 用户标识、本次交易金额、以及用户近期的交易频次。作为
 * {@link com.example.patterns.behavioral.chain.handler.RiskRuleHandler#handle} 的统一入参，
 * 使黑名单、金额上限、频次等不同规则节点在不改变调用方与链驱动代码的前提下自由增减与重排
 * （责任链模式的核心诉求，对应需求 4.4）。</p>
 *
 * <p>本类为纯数据对象（DTO），不承载任何风控判定逻辑；getter/setter 由 Lombok 的
 * {@link Data} 注解生成。各规则节点仅读取与自身相关的字段，未使用的字段允许为 {@code null}，
 * 由各节点自行做缺失值的宽松处理。金额采用 {@link BigDecimal} 表示，避免浮点类型在金额
 * 比较中引入精度误差（金融场景的基本要求）。</p>
 *
 * @since 1.0.0
 */
@Data
public class RiskContext {

    /**
     * 交易发起用户标识。
     *
     * <p>黑名单规则节点据此判断该用户是否处于风控黑名单。为空时视为非黑名单用户。</p>
     */
    private String userId;

    /**
     * 本次交易金额（单位：元）。
     *
     * <p>金额上限规则节点据此判断是否超过单笔交易限额。为空时视为不触发金额上限拦截。</p>
     */
    private BigDecimal amount;

    /**
     * 用户近期统计窗口内已发生的交易次数。
     *
     * <p>频次规则节点据此判断用户交易是否过于频繁。为空时视为不触发频次拦截。</p>
     */
    private Integer recentTransactionCount;
}
