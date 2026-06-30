package com.example.patterns.structural.facade;

import java.math.BigDecimal;

/**
 * 优惠子系统服务。
 *
 * <p>外观模式中的「子系统（SubSystem）」角色之一，专注于依据订单金额计算可享的优惠。
 * 该子系统对调用方隐藏：仅由 {@link OrderPlacementFacade} 在下单流程的第二步（扣减库存之后、
 * 发起支付之前）进行依赖与编排，业务调用方不直接接触本接口（迪米特法则）。</p>
 *
 * @since 1.0.0
 */
public interface PromotionSubSystemService {

    /**
     * 依据订单原始金额计算可享的优惠金额。
     *
     * <p>返回的优惠金额恒落在 {@code [0, originalAmount]} 区间内，保证优惠后金额非负；
     * 当传入金额为空或为负数时抛出 {@link com.example.patterns.common.exception.ServiceException}。</p>
     *
     * @param originalAmount 订单原始金额（即单价 × 数量，单位：元）
     * @return 优惠金额（单位：元），区间为 {@code [0, originalAmount]}
     */
    BigDecimal calculateDiscount(BigDecimal originalAmount);
}
