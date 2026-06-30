package com.example.patterns.behavioral.strategy.service;

import com.example.patterns.behavioral.strategy.domain.PromotionContext;
import com.example.patterns.behavioral.strategy.domain.PromotionResult;
import com.example.patterns.behavioral.strategy.strategy.PromotionStrategy;
import com.example.patterns.common.constant.HttpStatus;
import com.example.patterns.common.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 促销计算上下文服务。
 *
 * <p>策略模式中的「上下文（Context）」角色，负责依据传入的策略类型标识选取对应的
 * {@link PromotionStrategy} 实现并委派其完成优惠计算，使调用方无需感知具体策略类，
 * 仅依赖抽象接口与类型标识即可完成促销计算，新增策略也无需改动调用方代码（对应需求 4.1、10.4）。</p>
 *
 * <p>与 Spring 的结合方式：本服务以 {@link List} 形式注入容器中全部 {@link PromotionStrategy}
 * 实现，并在初始化阶段基于各实现自身声明的 {@link PromotionStrategy#type()} 建立
 * 「类型标识 → 策略」路由表。相较于直接注入以 bean 名为键的 {@code Map<String,PromotionStrategy>}，
 * 该方式不依赖 bean 命名约定，类型标识完全由实现类自治声明，新增策略只需新增实现类即可
 * 自动接入（满足开闭原则，对应需求 10.4）。</p>
 *
 * @since 1.0.0
 */
@Service
public class PromotionCalculateService {

    /**
     * 容器中注入的全部促销策略实现。
     *
     * <p>以 {@link List} 注入而非按 bean 名注入 {@code Map}，便于在初始化阶段基于各实现
     * 自身声明的类型标识建立路由表，避免对 bean 名称的隐式依赖。</p>
     */
    @Resource
    private List<PromotionStrategy> promotionStrategies;

    /**
     * 「类型标识 → 促销策略」路由表。
     *
     * <p>在容器完成依赖注入后由 {@link #initStrategyMap()} 一次性构建，运行期只读。</p>
     */
    private final Map<String, PromotionStrategy> strategyMap = new HashMap<>();

    /**
     * 在依赖注入完成后构建「类型标识 → 策略」路由表。
     *
     * <p>遍历全部注入的促销策略，以各自 {@link PromotionStrategy#type()} 为键登记入表。
     * 若出现重复的类型标识，说明存在配置冲突（同一类型有多个实现），此处快速失败抛出
     * {@link IllegalStateException}，将问题暴露在应用启动阶段而非运行期。</p>
     */
    @PostConstruct
    public void initStrategyMap() {
        for (PromotionStrategy strategy : promotionStrategies) {
            String type = strategy.type();
            PromotionStrategy existing = strategyMap.put(type, strategy);
            if (existing != null) {
                throw new IllegalStateException("存在重复的促销策略类型标识：" + type);
            }
        }
    }

    /**
     * 按策略类型标识计算促销优惠结果。
     *
     * <p>先校验促销上下文的合法性，再依据类型标识选取对应策略并委派其计算。未知类型标识
     * 或非法上下文均抛出 {@link ServiceException}，由全局异常处理器转换为可观察的错误响应
     * （非静默失败）。</p>
     *
     * @param type 促销策略类型标识，如 {@code "full_reduction"}、{@code "discount"}、{@code "direct_reduction"}
     * @param ctx  促销计算上下文，承载原始金额与各策略所需参数
     * @return 满足促销不变式的促销计算结果
     */
    public PromotionResult calculate(String type, PromotionContext ctx) {
        validateContext(ctx);
        PromotionStrategy strategy = selectStrategy(type);
        return strategy.calculate(ctx);
    }

    /**
     * 校验促销计算上下文的合法性。
     *
     * <p>上下文不能为空，原始金额不能为空且不能为负数；任一不满足即抛出 {@link ServiceException}。</p>
     *
     * @param ctx 待校验的促销计算上下文
     */
    private void validateContext(PromotionContext ctx) {
        if (ctx == null) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "促销计算上下文不能为空");
        }
        BigDecimal originalAmount = ctx.getOriginalAmount();
        if (originalAmount == null) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "原始金额不能为空");
        }
        if (originalAmount.signum() < 0) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "原始金额不能为负数");
        }
    }

    /**
     * 依据类型标识从路由表中选取对应的促销策略。
     *
     * <p>类型标识为空或不受支持时抛出 {@link ServiceException}，且不创建任何策略实例
     * （策略均为容器预先管理的单例，本方法仅做路由查找）。</p>
     *
     * @param type 促销策略类型标识
     * @return 与类型标识匹配的促销策略实例
     */
    private PromotionStrategy selectStrategy(String type) {
        if (!StringUtils.hasText(type)) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "促销策略类型标识不能为空");
        }
        PromotionStrategy strategy = strategyMap.get(type);
        if (strategy == null) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "不支持的促销策略类型：" + type);
        }
        return strategy;
    }
}
