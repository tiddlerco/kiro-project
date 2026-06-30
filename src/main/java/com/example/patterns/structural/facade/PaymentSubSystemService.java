package com.example.patterns.structural.facade;

import java.math.BigDecimal;

/**
 * 支付子系统服务。
 *
 * <p>外观模式中的「子系统（SubSystem）」角色之一，专注于按支付渠道发起一次收款。
 * 该子系统对调用方隐藏：仅由 {@link OrderPlacementFacade} 在下单流程的第三步（计算优惠之后）
 * 进行依赖与编排，业务调用方不直接接触本接口（迪米特法则）。</p>
 *
 * <p>说明：本支付子系统为外观演示内自洽的简化实现，与工厂方法模式中的支付处理器各自独立，
 * 以保证模式之间互不耦合。</p>
 *
 * @since 1.0.0
 */
public interface PaymentSubSystemService {

    /**
     * 通过指定渠道发起支付。
     *
     * <p>当支付渠道不受支持、应付金额非正或付款用户为空时，视为支付失败并抛出
     * {@link com.example.patterns.common.exception.ServiceException}，由全局异常处理器
     * 统一转换为可观察的错误响应；支付失败时不生成任何支付流水。</p>
     *
     * @param payChannel    支付渠道标识，如 {@code "wechat"}、{@code "alipay"}、{@code "balance"}
     * @param payableAmount 应付金额（单位：元，须为正数）
     * @param buyerId       付款用户标识
     * @return 支付成功后由渠道生成的唯一支付流水号
     */
    String pay(String payChannel, BigDecimal payableAmount, String buyerId);
}
