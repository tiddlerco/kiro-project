package com.example.patterns.behavioral.chain.domain;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 风控校验请求。
 *
 * <p>责任链模式演示接口 {@code POST /pattern/chain/riskCheck} 的入参对象，承载一次风控校验
 * 所需的全部输入：交易发起用户标识、本次交易金额、用户近期交易次数。其上的校验注解供控制器以
 * {@code @Validated} 触发声明式参数校验，避免在控制器中手写 if 校验逻辑（与项目统一的请求
 * 对象校验规范一致）。</p>
 *
 * <p>本类为纯数据对象（DTO），不承载任何风控判定逻辑；getter/setter 由 Lombok 的
 * {@link Data} 注解生成。金额采用 {@link BigDecimal} 表示，避免浮点类型在金额比较中引入
 * 精度误差。通过 {@link #toContext()} 一对一映射为 {@link RiskContext} 后交由责任链驱动，
 * 使控制器保持仅路由分发的职责。</p>
 *
 * @since 1.0.0
 */
@Data
public class RiskCheckRequest {

    /**
     * 交易发起用户标识。
     *
     * <p>黑名单规则节点据此判断该用户是否处于风控黑名单，必填。</p>
     */
    @NotBlank(message = "用户标识不能为空")
    private String userId;

    /**
     * 本次交易金额（单位：元）。
     *
     * <p>金额上限规则节点据此判断是否超过单笔交易限额，必填且业务上要求不小于 0。</p>
     */
    @NotNull(message = "交易金额不能为空")
    private BigDecimal amount;

    /**
     * 用户近期统计窗口内已发生的交易次数。
     *
     * <p>频次规则节点据此判断用户交易是否过于频繁，为可选项；为空时按不触发频次拦截处理。</p>
     */
    private Integer recentTransactionCount;

    /**
     * 将当前请求对象映射为风控校验上下文。
     *
     * <p>仅做字段一对一搬运，不承载任何风控业务判断（规则判定由责任链各节点负责），
     * 使控制器保持仅路由分发的职责。</p>
     *
     * @return 与本请求字段值一致的风控校验上下文
     */
    public RiskContext toContext() {
        RiskContext context = new RiskContext();
        context.setUserId(userId);
        context.setAmount(amount);
        context.setRecentTransactionCount(recentTransactionCount);
        return context;
    }
}
