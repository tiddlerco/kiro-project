package com.example.patterns.behavioral.state;

import com.example.patterns.behavioral.state.entity.OrderEntity;
import com.example.patterns.behavioral.state.mapper.OrderMapper;
import com.example.patterns.behavioral.state.service.OrderStateService;
import com.example.patterns.behavioral.state.state.CancelledState;
import com.example.patterns.behavioral.state.state.CompletedState;
import com.example.patterns.behavioral.state.state.CreatedState;
import com.example.patterns.behavioral.state.state.OrderState;
import com.example.patterns.behavioral.state.state.PaidState;
import com.example.patterns.behavioral.state.state.ShippedState;
import com.example.patterns.common.exception.IllegalStateTransitionException;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.GenerationMode;
import net.jqwik.api.Label;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 订单状态机合法与非法流转属性测试（对应任务 21.4，Property 12，验证需求 4.5、4.6）。
 *
 * <p>对「初始状态 × 动作」的全组合进行随机抽样，验证两条通用性质在任意组合下均成立：
 * 其一，合法流转会将订单转入由流转表唯一确定的目标状态；其二，非法流转会抛出
 * {@link IllegalStateTransitionException} 且订单状态保持不变。</p>
 *
 * <p>装配方式与单元测试一致：以 Mockito 模拟 {@link OrderMapper}（{@code selectById} 返回指定
 * 初始状态订单、{@code updateStatus} 返回 1），并通过 {@link ReflectionTestUtils} 注入全部
 * {@link OrderState} 实现后手动触发 {@link OrderStateService#initStateRegistry()}。</p>
 *
 * @since 1.0.0
 */
class OrderStateMachinePropertyTest {

    /**
     * 测试所用的订单主键 id。
     */
    private static final Long ORDER_ID = 1L;

    /**
     * 状态机的唯一合法流转表：键为「初始状态#动作」，值为唯一确定的目标状态。
     *
     * <p>覆盖设计约定的全部合法流转：CREATED→PAID/CANCELLED、PAID→SHIPPED/CANCELLED、
     * SHIPPED→COMPLETED；未出现在表中的「初始状态#动作」组合一律视为非法流转。</p>
     */
    private static final Map<String, String> LEGAL_TRANSITIONS = buildLegalTransitions();

    /**
     * 构建状态机的合法流转表。
     *
     * @return 「初始状态#动作」到唯一目标状态的映射
     */
    private static Map<String, String> buildLegalTransitions() {
        Map<String, String> transitions = new HashMap<>();
        transitions.put(OrderStatus.CREATED + "#pay", OrderStatus.PAID);
        transitions.put(OrderStatus.CREATED + "#cancel", OrderStatus.CANCELLED);
        transitions.put(OrderStatus.PAID + "#ship", OrderStatus.SHIPPED);
        transitions.put(OrderStatus.PAID + "#cancel", OrderStatus.CANCELLED);
        transitions.put(OrderStatus.SHIPPED + "#complete", OrderStatus.COMPLETED);
        return transitions;
    }

    /**
     * 生成订单初始状态码，覆盖状态机全部五个状态。
     *
     * @return 初始状态码生成器
     */
    @Provide
    Arbitrary<String> initialStates() {
        return Arbitraries.of(
                OrderStatus.CREATED,
                OrderStatus.PAID,
                OrderStatus.SHIPPED,
                OrderStatus.COMPLETED,
                OrderStatus.CANCELLED);
    }

    /**
     * 生成订单动作标识，覆盖状态机全部四个动作。
     *
     * @return 动作标识生成器
     */
    @Provide
    Arbitrary<String> orderActions() {
        return Arbitraries.of("pay", "ship", "complete", "cancel");
    }

    /**
     * 构建全部订单状态实现列表，模拟 Spring 容器注入的 {@code List<OrderState>}。
     *
     * @return 包含五个具体状态对象的列表
     */
    private List<OrderState> allOrderStates() {
        return Arrays.asList(
                new CreatedState(),
                new PaidState(),
                new ShippedState(),
                new CompletedState(),
                new CancelledState());
    }

    /**
     * 装配一个以 mock {@link OrderMapper} 为持久化后端、订单处于指定初始状态的状态服务。
     *
     * @param order {@code selectById} 应返回的订单实体
     * @return 完成路由表初始化的订单状态服务
     */
    private OrderStateService buildService(OrderEntity order) {
        OrderMapper orderMapper = mock(OrderMapper.class);
        when(orderMapper.selectById(anyLong())).thenReturn(order);
        when(orderMapper.updateStatus(anyLong(), anyString())).thenReturn(1);

        OrderStateService service = new OrderStateService();
        ReflectionTestUtils.setField(service, "orderStates", allOrderStates());
        ReflectionTestUtils.setField(service, "orderMapper", orderMapper);
        service.initStateRegistry();
        return service;
    }

    /**
     * 构造一个指定初始状态的订单实体。
     *
     * @param initialStatus 订单初始状态码
     * @return 主键为 {@link #ORDER_ID}、状态为入参的订单实体
     */
    private OrderEntity newOrder(String initialStatus) {
        OrderEntity order = new OrderEntity();
        order.setId(ORDER_ID);
        order.setStatus(initialStatus);
        return order;
    }

    // Feature: design-patterns-showcase, Property 12: 状态机合法与非法流转
    /**
     * Property 12：状态机合法与非法流转。
     *
     * <p><b>Validates: Requirements 4.5, 4.6</b></p>
     *
     * <p>对「初始状态 × 动作」全组合随机抽样：合法流转应转入由流转表唯一确定的目标状态；
     * 非法流转应抛出 {@link IllegalStateTransitionException} 且订单状态保持不变。</p>
     *
     * <p>说明：jqwik 引擎禁止 {@code @Property} 方法携带 JUnit 的 {@code @DisplayName} 注解
     * （在本工程 surefire 2.22.2 下会导致属性方法被跳过而不执行），故此处改用 jqwik 提供的
     * 等价注解 {@link Label} 标注中文描述，与工程内其余属性测试保持一致。</p>
     *
     * @param initialStatus 随机生成的订单初始状态码
     * @param action        随机生成的订单动作标识
     */
    @Property(tries = 100, generation = GenerationMode.RANDOMIZED)
    @Label("合法流转转入唯一目标状态，非法流转拒绝且状态不变")
    void legalTransitionReachesUniqueTargetAndIllegalTransitionRejected(
            @ForAll("initialStates") String initialStatus,
            @ForAll("orderActions") String action) {
        OrderEntity order = newOrder(initialStatus);
        OrderStateService service = buildService(order);
        String expectedTarget = LEGAL_TRANSITIONS.get(initialStatus + "#" + action);

        if (expectedTarget != null) {
            OrderEntity result = service.changeStatus(ORDER_ID, action);
            assertThat(result.getStatus())
                    .as("合法流转 %s + %s 应转入唯一目标状态 %s", initialStatus, action, expectedTarget)
                    .isEqualTo(expectedTarget);
        } else {
            assertThatThrownBy(() -> service.changeStatus(ORDER_ID, action))
                    .as("非法流转 %s + %s 应被拒绝", initialStatus, action)
                    .isInstanceOf(IllegalStateTransitionException.class);
            assertThat(order.getStatus())
                    .as("非法流转 %s + %s 后订单状态应保持不变", initialStatus, action)
                    .isEqualTo(initialStatus);
        }
    }
}
