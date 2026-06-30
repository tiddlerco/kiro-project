package com.example.patterns.structural.facade;

import com.example.patterns.common.constant.HttpStatus;
import com.example.patterns.common.exception.ServiceException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 优惠子系统服务实现。
 *
 * <p>外观模式中的「具体子系统」实现之一，依据订单原始金额按「阶梯满减」规则计算优惠：
 * 金额越高减免越多。各档减免额均小于其对应的满减门槛，因而优惠金额恒不超过订单原始金额，
 * 保证优惠后金额非负。</p>
 *
 * @since 1.0.0
 */
@Service
public class PromotionSubSystemServiceImpl implements PromotionSubSystemService {

    /**
     * 高额档满减门槛（单位：元）。
     */
    private static final BigDecimal THRESHOLD_HIGH = new BigDecimal("500");

    /**
     * 高额档减免金额（单位：元）。
     */
    private static final BigDecimal DISCOUNT_HIGH = new BigDecimal("100");

    /**
     * 中额档满减门槛（单位：元）。
     */
    private static final BigDecimal THRESHOLD_MEDIUM = new BigDecimal("200");

    /**
     * 中额档减免金额（单位：元）。
     */
    private static final BigDecimal DISCOUNT_MEDIUM = new BigDecimal("30");

    /**
     * 低额档满减门槛（单位：元）。
     */
    private static final BigDecimal THRESHOLD_LOW = new BigDecimal("100");

    /**
     * 低额档减免金额（单位：元）。
     */
    private static final BigDecimal DISCOUNT_LOW = new BigDecimal("10");

    /**
     * 依据订单原始金额计算可享的优惠金额。
     *
     * <p>按金额从高到低依次匹配满减门槛，命中即返回对应档位的减免金额；未达最低门槛则无优惠。
     * 当传入金额为空或为负数时抛出 {@link ServiceException}。</p>
     *
     * @param originalAmount 订单原始金额（即单价 × 数量，单位：元）
     * @return 优惠金额（单位：元），区间为 {@code [0, originalAmount]}
     */
    @Override
    public BigDecimal calculateDiscount(BigDecimal originalAmount) {
        if (originalAmount == null || originalAmount.signum() < 0) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "订单原始金额不合法");
        }
        if (originalAmount.compareTo(THRESHOLD_HIGH) >= 0) {
            return DISCOUNT_HIGH;
        }
        if (originalAmount.compareTo(THRESHOLD_MEDIUM) >= 0) {
            return DISCOUNT_MEDIUM;
        }
        if (originalAmount.compareTo(THRESHOLD_LOW) >= 0) {
            return DISCOUNT_LOW;
        }
        return BigDecimal.ZERO;
    }
}
