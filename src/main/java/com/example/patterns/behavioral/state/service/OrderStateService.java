package com.example.patterns.behavioral.state.service;

import com.example.patterns.behavioral.state.OrderStateContext;
import com.example.patterns.behavioral.state.entity.OrderEntity;
import com.example.patterns.behavioral.state.mapper.OrderMapper;
import com.example.patterns.behavioral.state.state.OrderState;
import com.example.patterns.common.constant.HttpStatus;
import com.example.patterns.common.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 订单状态机组装服务（State 模式中「上下文」的装配与统一入口）。
 *
 * <p>本服务承担两项职责：其一，在应用启动阶段，将容器中全部 {@link OrderState} 具体状态 Bean
 * 装配为「状态码 → 状态对象」路由表 {@link #stateRegistry}，供按订单当前状态码解析初始状态使用；
 * 其二，对外提供 {@link #changeStatus(Long, String)} 作为订单状态流转的统一入口，依据动作标识
 * 创建有状态的 {@link OrderStateContext} 并委派其完成动作分发，从而把「不同状态下同一动作的差异行为」
 * 完全交由各具体状态类承载（State 模式核心），调用方无需感知任何状态判断。</p>
 *
 * <p>与 Spring 的结合方式：以 {@link List} 形式注入容器中全部 {@link OrderState} 实现，并基于各实现
 * 自身声明的 {@link OrderState#stateName()} 建立路由表，不依赖 Bean 命名约定，新增状态只需新增标注
 * {@code @Component} 的状态类即可自动接入（满足开闭原则）。状态对象均为无内部可变字段的单例，可安全共享；
 * 而 {@link OrderStateContext} 为「每个订单一个实例」的有状态对象，故在每次流转时按订单临时创建。</p>
 *
 * @since 1.0.0
 */
@Service
public class OrderStateService {

    /**
     * 容器中注入的全部订单状态实现。
     *
     * <p>以 {@link List} 注入而非按 Bean 名注入 {@code Map}，便于在初始化阶段基于各实现自身声明的
     * 状态码建立路由表，避免对 Bean 名称的隐式依赖。</p>
     */
    @Resource
    private List<OrderState> orderStates;

    /**
     * 订单持久化 Mapper，用于查询订单及在合法流转后写回新状态。
     */
    @Resource
    private OrderMapper orderMapper;

    /**
     * 「状态码 → 状态对象」路由表。
     *
     * <p>在容器完成依赖注入后由 {@link #initStateRegistry()} 一次性构建，运行期只读，
     * 由各订单的 {@link OrderStateContext} 复用以解析状态对象。</p>
     */
    private final Map<String, OrderState> stateRegistry = new HashMap<>();

    /**
     * 在依赖注入完成后构建「状态码 → 状态对象」路由表。
     *
     * <p>遍历全部注入的订单状态对象，以各自 {@link OrderState#stateName()} 为键登记入表。
     * 若出现重复的状态码，说明存在配置冲突（同一状态有多个实现），此处快速失败抛出
     * {@link IllegalStateException}，将问题暴露在应用启动阶段而非运行期。</p>
     */
    @PostConstruct
    public void initStateRegistry() {
        for (OrderState orderState : orderStates) {
            String stateName = orderState.stateName();
            OrderState existing = stateRegistry.put(stateName, orderState);
            if (existing != null) {
                throw new IllegalStateException("存在重复的订单状态码：" + stateName);
            }
        }
    }

    /**
     * 执行一次订单状态流转：依据动作标识驱动订单从当前状态流转至下一状态。
     *
     * <p>先按主键查询订单，订单不存在时抛出 {@link ServiceException}（资源不存在）；再以该订单与路由表
     * 创建 {@link OrderStateContext}，并将动作分发给当前状态对象处理——合法流转将持久化并切换状态，
     * 非法流转将抛出非法状态流转异常且订单状态保持不变。最终返回承载最新状态的订单实体。</p>
     *
     * @param orderId 订单主键 id
     * @param action  订单动作标识，取值为 {@code pay}（支付）/{@code ship}（发货）/{@code complete}（完成）/{@code cancel}（取消），不区分大小写
     * @return 流转处理后的订单实体，其状态字段反映本次流转后的最新状态
     */
    public OrderEntity changeStatus(Long orderId, String action) {
        OrderEntity order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new ServiceException(HttpStatus.NOT_FOUND, "订单不存在，订单 id：" + orderId);
        }
        OrderStateContext context = new OrderStateContext(order, stateRegistry, orderMapper);
        dispatch(context, action);
        return context.getOrder();
    }

    /**
     * 将订单动作分发给状态上下文对应的动作入口。
     *
     * <p>按动作标识委派至 {@link OrderStateContext} 的支付、发货、完成、取消入口；动作标识为空或
     * 不受支持时抛出 {@link ServiceException}（请求参数错误），由全局异常处理器转换为可观察的错误响应
     * （非静默失败）。</p>
     *
     * @param context 订单状态上下文，承载当前订单与状态并提供各动作入口
     * @param action  订单动作标识，取值为 {@code pay}/{@code ship}/{@code complete}/{@code cancel}，不区分大小写
     */
    private void dispatch(OrderStateContext context, String action) {
        if (!StringUtils.hasText(action)) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "订单动作不能为空");
        }
        // 修改说明：按任务契约要求动作标识「不区分大小写」，此处先去除首尾空白并统一转为小写后再匹配，
        // 以兼容调用方传入的 PAY / Pay / pay 等不同写法；使用 Locale.ROOT 规避区域相关的大小写转换差异（如土耳其语 i）。
        String normalizedAction = action.trim().toLowerCase(Locale.ROOT);
        switch (normalizedAction) {
            case "pay":
                context.pay();
                break;
            case "ship":
                context.ship();
                break;
            case "complete":
                context.complete();
                break;
            case "cancel":
                context.cancel();
                break;
            default:
                throw new ServiceException(HttpStatus.BAD_REQUEST, "不支持的订单动作：" + action);
        }
    }
}
