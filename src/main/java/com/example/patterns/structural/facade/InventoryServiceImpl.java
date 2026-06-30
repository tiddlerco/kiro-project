package com.example.patterns.structural.facade;

import com.example.patterns.common.constant.HttpStatus;
import com.example.patterns.common.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 库存子系统服务实现。
 *
 * <p>外观模式中的「具体子系统」实现之一，以内存表模拟商品库存的检查与扣减。
 * 为演示「库存不足」这一失败路径，初始化时刻意预置了一个零库存商品。</p>
 *
 * <p>库存表采用 {@link ConcurrentHashMap} 承载，扣减通过其原子的
 * {@link ConcurrentHashMap#compute} 完成「检查—扣减」的复合操作，保证并发下不会出现
 * 超卖（检查与扣减之间的竞态）。</p>
 *
 * @since 1.0.0
 */
@Service
public class InventoryServiceImpl implements InventoryService {

    /**
     * 内存库存表：商品编码 → 当前可售库存。
     *
     * <p>使用 {@link ConcurrentHashMap} 以支持并发安全的原子扣减。</p>
     */
    private final Map<String, Integer> stockTable = new ConcurrentHashMap<>();

    /**
     * 构造库存子系统并预置演示用初始库存。
     *
     * <p>预置三个商品，其中 {@code P1003} 库存为 0，用于演示「库存不足」时下单被拒的失败路径。</p>
     */
    public InventoryServiceImpl() {
        stockTable.put("P1001", 100);
        stockTable.put("P1002", 50);
        stockTable.put("P1003", 0);
    }

    /**
     * 检查指定商品的库存是否足以满足期望扣减的数量。
     *
     * <p>当商品不存在时视为库存不足，返回 {@code false}；不产生任何扣减副作用。</p>
     *
     * @param productCode 商品编码
     * @param quantity    期望扣减的数量
     * @return 库存充足返回 {@code true}，库存不足或商品不存在返回 {@code false}
     */
    @Override
    public boolean hasEnoughStock(String productCode, int quantity) {
        Integer current = stockTable.get(productCode);
        return current != null && current >= quantity;
    }

    /**
     * 扣减指定商品的库存。
     *
     * <p>先校验入参合法性，再以原子方式完成「检查库存是否充足—扣减」。当商品不存在或库存不足时
     * 抛出 {@link ServiceException}，且库存状态保持不变（原子操作内抛出异常不会写回新值）。</p>
     *
     * @param productCode 商品编码
     * @param quantity    扣减数量（须为正整数）
     * @return 扣减成功后该商品的剩余库存
     */
    @Override
    public int deduct(String productCode, int quantity) {
        if (!StringUtils.hasText(productCode)) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "商品编码不能为空");
        }
        if (quantity <= 0) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "扣减数量必须大于 0");
        }
        return stockTable.compute(productCode, (code, current) -> {
            if (current == null) {
                throw new ServiceException(HttpStatus.NOT_FOUND, "商品不存在或未配置库存：" + code);
            }
            if (current < quantity) {
                throw new ServiceException(HttpStatus.BAD_REQUEST,
                        "商品库存不足，商品：" + code + "，剩余：" + current + "，需求：" + quantity);
            }
            return current - quantity;
        });
    }
}
