package com.example.patterns.structural.facade;

import com.example.patterns.common.constant.HttpStatus;
import com.example.patterns.common.exception.ServiceException;
import com.example.patterns.structural.facade.domain.PlaceOrderRequest;
import com.example.patterns.structural.facade.domain.PlaceOrderResult;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 下单外观。
 *
 * <p>外观模式中的「外观（Facade）」角色，为复杂的下单流程提供单一、简化的入口。
 * 它将库存、优惠、支付三个相互独立的子系统编排为一次连贯的下单调用，调用方只需依赖本类的
 * {@link #placeOrder} 方法，而无需了解、也无需直接依赖任何子系统（迪米特法则 / 最少知识原则）。</p>
 *
 * <p>编排顺序固定为：扣减库存 → 计算优惠 → 发起支付（对应需求 3.4）。三个子系统均以接口类型注入，
 * 外观仅依赖抽象而非具体实现（依赖倒置），任一子系统失败都会抛出
 * {@link ServiceException}，由全局异常处理器统一转换为可观察的错误响应，绝不静默失败。</p>
 *
 * <p>风险提示：本示例聚焦外观模式「编排多子系统」这一核心职责，未引入跨子系统的补偿/回滚机制。
 * 因此当库存扣减成功而后续支付失败时，已扣减的库存不会在本流程内自动回补；真实生产场景应结合
 * 本地事务消息、TCC 或可靠事件等手段保证最终一致性，此处从略以突出模式本身。</p>
 *
 * @since 1.0.0
 */
@Service
public class OrderPlacementFacade {

    /**
     * 订单号前缀。
     */
    private static final String ORDER_NO_PREFIX = "ORD";

    /**
     * 金额计算保留的小数位数。
     */
    private static final int AMOUNT_SCALE = 2;

    /**
     * 库存子系统，负责扣减库存（下单流程第一步）。
     */
    @Resource
    private InventoryService inventoryService;

    /**
     * 优惠子系统，负责计算优惠（下单流程第二步）。
     */
    @Resource
    private PromotionSubSystemService promotionSubSystemService;

    /**
     * 支付子系统，负责发起支付（下单流程第三步）。
     */
    @Resource
    private PaymentSubSystemService paymentSubSystemService;

    /**
     * 下单：以单一入口编排库存、优惠、支付三个子系统完成整套下单流程。
     *
     * <p>处理步骤：校验请求 → 计算原始金额 → 扣减库存 → 计算优惠 → 计算实付金额 → 发起支付 →
     * 组装下单结果。任一步骤失败均以 {@link ServiceException} 中断并向上抛出。</p>
     *
     * @param request 下单请求，承载商品、数量、单价、下单用户与支付渠道
     * @return 下单结果，包含订单号、各项金额、剩余库存与支付流水号等可观察信息
     */
    public PlaceOrderResult placeOrder(PlaceOrderRequest request) {
        validateRequest(request);
        BigDecimal originalAmount = calculateOriginalAmount(request);
        int remainingStock = inventoryService.deduct(request.getProductCode(), request.getQuantity());
        BigDecimal discountAmount = promotionSubSystemService.calculateDiscount(originalAmount);
        BigDecimal payableAmount = originalAmount.subtract(discountAmount);
        String transactionId = paymentSubSystemService.pay(request.getPayChannel(), payableAmount, request.getBuyerId());
        return buildResult(request, originalAmount, discountAmount, payableAmount, remainingStock, transactionId);
    }

    /**
     * 校验下单请求的基本合法性。
     *
     * <p>对必填字段与数值边界做防御性校验，任一不满足即抛出 {@link ServiceException}，
     * 避免非法请求进入后续子系统编排。</p>
     *
     * @param request 待校验的下单请求
     */
    private void validateRequest(PlaceOrderRequest request) {
        if (request == null) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "下单请求不能为空");
        }
        if (!StringUtils.hasText(request.getProductCode())) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "商品编码不能为空");
        }
        if (request.getQuantity() <= 0) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "购买数量必须大于 0");
        }
        if (request.getUnitPrice() == null || request.getUnitPrice().signum() <= 0) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "商品单价必须大于 0");
        }
        if (!StringUtils.hasText(request.getPayChannel())) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "支付渠道不能为空");
        }
        if (!StringUtils.hasText(request.getBuyerId())) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "下单用户不能为空");
        }
    }

    /**
     * 计算订单原始金额。
     *
     * <p>以「单价 × 数量」计算并保留两位小数（四舍五入），作为计算优惠前的应付金额。</p>
     *
     * @param request 下单请求，提供单价与数量
     * @return 订单原始金额（单位：元，保留两位小数）
     */
    private BigDecimal calculateOriginalAmount(PlaceOrderRequest request) {
        return request.getUnitPrice()
                .multiply(BigDecimal.valueOf(request.getQuantity()))
                .setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * 组装下单结果。
     *
     * <p>汇总三个子系统的处理产物与本次下单的金额信息，生成订单号与下单时间，构建可观察的下单结果。</p>
     *
     * @param request        下单请求，提供商品编码、数量与支付渠道
     * @param originalAmount 订单原始金额
     * @param discountAmount 优惠金额
     * @param payableAmount  实付金额
     * @param remainingStock 扣减库存后的剩余库存
     * @param transactionId  支付流水号
     * @return 字段填充完成的下单结果
     */
    private PlaceOrderResult buildResult(PlaceOrderRequest request, BigDecimal originalAmount,
                                         BigDecimal discountAmount, BigDecimal payableAmount,
                                         int remainingStock, String transactionId) {
        PlaceOrderResult result = new PlaceOrderResult();
        result.setOrderNo(generateOrderNo());
        result.setProductCode(request.getProductCode());
        result.setQuantity(request.getQuantity());
        result.setOriginalAmount(originalAmount);
        result.setDiscountAmount(discountAmount);
        result.setPayableAmount(payableAmount);
        result.setRemainingStock(remainingStock);
        result.setPayChannel(request.getPayChannel());
        result.setTransactionId(transactionId);
        result.setPlaceTime(LocalDateTime.now());
        return result;
    }

    /**
     * 生成唯一订单号。
     *
     * <p>以固定前缀拼接去除分隔符并转为大写的 UUID，作为本次下单的业务单据编号。</p>
     *
     * @return 以 {@value #ORDER_NO_PREFIX} 开头的全局唯一订单号
     */
    private String generateOrderNo() {
        return ORDER_NO_PREFIX + UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }
}
