package com.example.patterns.structural.facade;

import com.example.patterns.common.constant.HttpStatus;
import com.example.patterns.common.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 支付子系统服务实现。
 *
 * <p>外观模式中的「具体子系统」实现之一，模拟按渠道发起收款：校验渠道与金额后生成支付流水号。
 * 为演示「支付失败」这一失败路径，对不受支持的渠道、非正的应付金额与空付款用户均判为失败并报错。</p>
 *
 * <p>本支付子系统为外观演示内自洽的简化实现，与工厂方法模式中的支付处理器各自独立，互不耦合。</p>
 *
 * @since 1.0.0
 */
@Service
public class PaymentSubSystemServiceImpl implements PaymentSubSystemService {

    /**
     * 受支持的支付渠道集合。
     *
     * <p>以不可变集合承载，避免运行期被意外修改。</p>
     */
    private static final Set<String> SUPPORTED_CHANNELS =
            Collections.unmodifiableSet(new HashSet<>(Arrays.asList("wechat", "alipay", "balance")));

    /**
     * 支付流水号前缀。
     */
    private static final String TRANSACTION_PREFIX = "PAY";

    /**
     * 通过指定渠道发起支付。
     *
     * <p>依次校验支付渠道、应付金额与付款用户，任一不满足即抛出 {@link ServiceException}；
     * 全部通过后生成并返回唯一支付流水号。</p>
     *
     * @param payChannel    支付渠道标识，如 {@code "wechat"}、{@code "alipay"}、{@code "balance"}
     * @param payableAmount 应付金额（单位：元，须为正数）
     * @param buyerId       付款用户标识
     * @return 支付成功后由渠道生成的唯一支付流水号
     */
    @Override
    public String pay(String payChannel, BigDecimal payableAmount, String buyerId) {
        if (!StringUtils.hasText(payChannel) || !SUPPORTED_CHANNELS.contains(payChannel)) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "不支持的支付渠道：" + payChannel);
        }
        if (payableAmount == null || payableAmount.signum() <= 0) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "应付金额必须大于 0");
        }
        if (!StringUtils.hasText(buyerId)) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "付款用户不能为空");
        }
        return generateTransactionId();
    }

    /**
     * 生成唯一支付流水号。
     *
     * <p>以固定前缀拼接去除分隔符并转为大写的 UUID，模拟渠道侧返回的唯一交易凭证编号。</p>
     *
     * @return 以 {@value #TRANSACTION_PREFIX} 开头的全局唯一支付流水号
     */
    private String generateTransactionId() {
        return TRANSACTION_PREFIX + UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }
}
