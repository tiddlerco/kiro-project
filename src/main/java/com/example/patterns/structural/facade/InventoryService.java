package com.example.patterns.structural.facade;

/**
 * 库存子系统服务。
 *
 * <p>外观模式中的「子系统（SubSystem）」角色之一，专注于商品库存的检查与扣减能力。
 * 该子系统对调用方隐藏：仅由 {@link OrderPlacementFacade} 在下单流程的第一步进行依赖与编排，
 * 业务调用方不直接接触本接口（迪米特法则）。</p>
 *
 * <p>以接口形式对外暴露能力、由具体实现承载逻辑，使外观仅依赖抽象而非具体实现
 * （依赖倒置），便于后续替换为真实的库存中心或数据库实现。</p>
 *
 * @since 1.0.0
 */
public interface InventoryService {

    /**
     * 检查指定商品的库存是否足以满足期望扣减的数量。
     *
     * <p>本方法为只读探测，不产生任何扣减副作用，可在正式扣减前用于预判。</p>
     *
     * @param productCode 商品编码
     * @param quantity    期望扣减的数量
     * @return 库存充足返回 {@code true}，库存不足或商品不存在返回 {@code false}
     */
    boolean hasEnoughStock(String productCode, int quantity);

    /**
     * 扣减指定商品的库存。
     *
     * <p>当商品不存在或库存不足时抛出 {@link com.example.patterns.common.exception.ServiceException}，
     * 由全局异常处理器统一转换为可观察的错误响应；扣减失败时不改变任何库存状态。</p>
     *
     * @param productCode 商品编码
     * @param quantity    扣减数量（须为正整数）
     * @return 扣减成功后该商品的剩余库存
     */
    int deduct(String productCode, int quantity);
}
