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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 订单状态机核心行为单元测试（对应任务 21.3，验证需求 4.5、4.6）。
 *
 * <p>聚焦 {@link OrderStateService#changeStatus(Long, String)} 的状态流转规则本身：通过 Mockito
 * 模拟 {@link OrderMapper}（{@code selectById} 返回指定初始状态的订单、{@code updateStatus} 返回 1），
 * 使测试不依赖真实数据库，只验证「合法流转转入目标状态、非法流转拒绝且状态不变」。</p>
 *
 * <p>装配方式：以 {@link ReflectionTestUtils} 注入容器中全部 {@link OrderState} 实现与 mock 的
 * {@link OrderMapper}，随后手动触发 {@link OrderStateService#initStateRegistry()}（模拟其
 * {@code @PostConstruct} 初始化），构建「状态码 → 状态对象」路由表。</p>
 *
 * @since 1.0.0
 */
class OrderStateServiceTest {

    /**
     * 测试所用的订单主键 id。
     */
    private static final Long ORDER_ID = 1L;

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

    /**
     * 装配一个以 mock {@link OrderMapper} 为持久化后端的订单状态服务。
     *
     * <p>{@code selectById} 固定返回入参订单实体（同一引用，便于断言状态是否被改动），
     * {@code updateStatus} 固定返回受影响行数 1（模拟持久化成功）。</p>
     *
     * @param order       {@code selectById} 应返回的订单实体
     * @param orderMapper 供后续 verify 的 mock Mapper
     * @return 完成路由表初始化的订单状态服务
     */
    private OrderStateService buildService(OrderEntity order, OrderMapper orderMapper) {
        when(orderMapper.selectById(anyLong())).thenReturn(order);
        when(orderMapper.updateStatus(anyLong(), anyString())).thenReturn(1);

        OrderStateService service = new OrderStateService();
        ReflectionTestUtils.setField(service, "orderStates", allOrderStates());
        ReflectionTestUtils.setField(service, "orderMapper", orderMapper);
        service.initStateRegistry();
        return service;
    }

    @ParameterizedTest(name = "{0} + {1} -> {2}")
    @CsvSource({
            "CREATED, pay, PAID",
            "PAID, ship, SHIPPED",
            "SHIPPED, complete, COMPLETED",
            "CREATED, cancel, CANCELLED",
            "PAID, cancel, CANCELLED"
    })
    @DisplayName("合法流转应转入目标状态并持久化新状态")
    void shouldTransitionToTargetStateWhenTransitionLegal(String initialStatus,
                                                          String action,
                                                          String expectedStatus) {
        OrderEntity order = newOrder(initialStatus);
        OrderMapper orderMapper = mock(OrderMapper.class);
        OrderStateService service = buildService(order, orderMapper);

        OrderEntity result = service.changeStatus(ORDER_ID, action);

        assertThat(result.getStatus()).isEqualTo(expectedStatus);
        verify(orderMapper, times(1)).updateStatus(ORDER_ID, expectedStatus);
    }

    @ParameterizedTest(name = "{0} + {1} -> 拒绝")
    @CsvSource({
            "CREATED, ship",
            "CREATED, complete",
            "COMPLETED, pay",
            "COMPLETED, ship",
            "COMPLETED, complete",
            "COMPLETED, cancel",
            "CANCELLED, pay",
            "CANCELLED, ship",
            "CANCELLED, complete",
            "CANCELLED, cancel"
    })
    @DisplayName("非法流转应抛出非法状态流转异常且订单状态保持不变")
    void shouldRejectAndKeepStateWhenTransitionIllegal(String initialStatus, String action) {
        OrderEntity order = newOrder(initialStatus);
        OrderMapper orderMapper = mock(OrderMapper.class);
        OrderStateService service = buildService(order, orderMapper);

        assertThatThrownBy(() -> service.changeStatus(ORDER_ID, action))
                .isInstanceOf(IllegalStateTransitionException.class);

        assertThat(order.getStatus()).isEqualTo(initialStatus);
        verify(orderMapper, never()).updateStatus(anyLong(), anyString());
    }
}
