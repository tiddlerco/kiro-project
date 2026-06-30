package com.example.patterns.behavioral.state.mapper;

import com.example.patterns.behavioral.state.entity.OrderEntity;
import org.apache.ibatis.annotations.Param;

/**
 * 订单 Mapper（状态模式 State 的持久化访问接口）。
 *
 * <p>本接口位于 {@code ...state.mapper} 包，由启动类上的 {@code @MapperScan("com.example.patterns.**.mapper")}
 * 自动扫描注册为 MyBatis 映射器，无需额外标注 {@code @Mapper}。所有 SQL 语句均外置于
 * {@code classpath:mapper/state/OrderMapper.xml}，遵循团队 MyBatis 规范（C11：SQL 写在 XML、
 * 禁止使用 MyBatis-Plus 的 Wrapper），满足需求 9.8、9.9。</p>
 *
 * @since 1.0.0
 */
public interface OrderMapper {

    /**
     * 按主键查询订单。
     *
     * @param id 订单主键 id
     * @return 匹配的订单实体；当不存在对应记录时返回 {@code null}
     */
    OrderEntity selectById(Long id);

    /**
     * 更新订单状态（状态机合法流转成功后调用，同时刷新更新时间）。
     *
     * @param id     订单主键 id
     * @param status 目标状态码，取值见 {@link com.example.patterns.behavioral.state.OrderStatus}
     * @return 受影响的行数；正常更新返回 1，订单不存在返回 0
     */
    int updateStatus(@Param("id") Long id, @Param("status") String status);
}
