package com.example.patterns.behavioral.chain.service;

import com.example.patterns.behavioral.chain.domain.RiskCheckResult;
import com.example.patterns.behavioral.chain.domain.RiskContext;
import com.example.patterns.behavioral.chain.handler.RiskRuleHandler;
import com.example.patterns.common.constant.HttpStatus;
import com.example.patterns.common.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 风控规则链装配与驱动服务。
 *
 * <p>责任链模式中负责「链装配与驱动」的角色，承担两项职责：其一，在容器完成依赖注入后将全部
 * {@link RiskRuleHandler} 节点按各自声明的 {@link RiskRuleHandler#order()} 升序组装为一条
 * 有序责任链；其二，驱动风控请求沿链依次传递，直至被某一节点拦截而短路，或顺利通过全部节点
 * （对应需求 4.4）。</p>
 *
 * <p>与 Spring 的结合方式：本服务以 {@link List} 形式注入容器中全部 {@link RiskRuleHandler}
 * 实现，新增规则节点只需新增一个 {@code @Component} 实现类并赋予唯一排序序号即可自动接入链路，
 * 无需改动本服务与调用方代码（满足开闭原则）。链的「短路传递」控制权集中在本服务，各节点保持
 * 无状态、互不引用，可单独测试与任意重排。</p>
 *
 * @since 1.0.0
 */
@Service
public class RiskRuleChain {

    /**
     * 容器中注入的全部风控规则处理器。
     *
     * <p>以 {@link List} 注入便于在初始化阶段统一排序组链，运行期不直接遍历该原始列表。</p>
     */
    @Resource
    private List<RiskRuleHandler> riskRuleHandlers;

    /**
     * 按 {@link RiskRuleHandler#order()} 升序排列后的有序责任链。
     *
     * <p>在容器完成依赖注入后由 {@link #initChain()} 一次性构建，运行期只读，
     * 驱动请求时按此顺序依次传递。</p>
     */
    private final List<RiskRuleHandler> orderedHandlers = new ArrayList<>();

    /**
     * 在依赖注入完成后按排序序号组装有序责任链。
     *
     * <p>将注入的全部处理器按 {@link RiskRuleHandler#order()} 升序排序后登记入链；同时校验
     * 排序序号的唯一性，若存在重复序号则说明链路顺序存在二义性，此处快速失败抛出
     * {@link IllegalStateException}，将配置问题暴露在应用启动阶段而非运行期。</p>
     */
    @PostConstruct
    public void initChain() {
        if (CollectionUtils.isEmpty(riskRuleHandlers)) {
            return;
        }
        List<RiskRuleHandler> sortedHandlers = new ArrayList<>(riskRuleHandlers);
        sortedHandlers.sort(Comparator.comparingInt(RiskRuleHandler::order));
        verifyOrderUnique(sortedHandlers);
        orderedHandlers.addAll(sortedHandlers);
    }

    /**
     * 校验各处理器的排序序号互不相同。
     *
     * <p>遍历已排序的处理器列表，借助集合记录已出现的序号，一旦出现重复即抛出
     * {@link IllegalStateException}，避免链路顺序产生二义性。</p>
     *
     * @param sortedHandlers 已按排序序号升序排列的处理器列表
     */
    private void verifyOrderUnique(List<RiskRuleHandler> sortedHandlers) {
        Set<Integer> seenOrders = new HashSet<>();
        for (RiskRuleHandler handler : sortedHandlers) {
            int order = handler.order();
            if (!seenOrders.add(order)) {
                throw new IllegalStateException("存在重复的风控规则节点排序序号：" + order);
            }
        }
    }

    /**
     * 驱动风控请求沿责任链依次校验。
     *
     * <p>先校验上下文合法性，再按组链顺序依次调用各节点的
     * {@link RiskRuleHandler#handle(RiskContext)}：一旦某节点返回「拦截」结果立即短路返回，
     * 其后节点不再执行；若全部节点均放行，则返回「通过」结果。该行为保证「结果为通过当且仅当
     * 所有节点均通过，且被拦截后其后节点不再执行」（对应需求 4.4）。</p>
     *
     * @param ctx 待校验的风控上下文
     * @return 整条链的校验结果：被拦截时携带命中节点与拦截原因，否则为通过结果
     */
    public RiskCheckResult check(RiskContext ctx) {
        validateContext(ctx);
        for (RiskRuleHandler handler : orderedHandlers) {
            RiskCheckResult result = handler.handle(ctx);
            if (!result.isPassed()) {
                return result;
            }
        }
        return RiskCheckResult.pass();
    }

    /**
     * 校验风控上下文的合法性。
     *
     * <p>上下文不能为空，为空时抛出 {@link ServiceException}，由全局异常处理器转换为可观察的
     * 错误响应（非静默失败）。</p>
     *
     * @param ctx 待校验的风控上下文
     */
    private void validateContext(RiskContext ctx) {
        if (ctx == null) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "风控校验上下文不能为空");
        }
    }
}
