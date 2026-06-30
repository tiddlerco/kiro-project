/**
 * 外观（Facade）模式示例包。
 *
 * <p>业务场景：下单流程通过单一外观接口编排库存、优惠、支付至少 3 个子系统，
 * 使调用方无需直接依赖各子系统（对应需求 3.4）。外观将「扣减库存 → 计算优惠 → 发起支付」
 * 这一多步骤、跨子系统的复杂流程，收敛为一次简单的下单调用。</p>
 *
 * <p>角色与对应类：</p>
 * <ul>
 *     <li>外观 Facade：{@link com.example.patterns.structural.facade.OrderPlacementFacade}</li>
 *     <li>子系统 SubSystem（库存）：{@link com.example.patterns.structural.facade.InventoryService}
 *         及其实现 {@link com.example.patterns.structural.facade.InventoryServiceImpl}</li>
 *     <li>子系统 SubSystem（优惠）：{@link com.example.patterns.structural.facade.PromotionSubSystemService}
 *         及其实现 {@link com.example.patterns.structural.facade.PromotionSubSystemServiceImpl}</li>
 *     <li>子系统 SubSystem（支付）：{@link com.example.patterns.structural.facade.PaymentSubSystemService}
 *         及其实现 {@link com.example.patterns.structural.facade.PaymentSubSystemServiceImpl}</li>
 *     <li>领域数据对象：{@link com.example.patterns.structural.facade.domain.PlaceOrderRequest}、
 *         {@link com.example.patterns.structural.facade.domain.PlaceOrderResult}</li>
 * </ul>
 *
 * <p>与 Spring 的结合：各子系统以 {@code @Service} 交由容器管理，外观以接口类型注入三个子系统、
 * 仅依赖抽象而非具体实现（依赖倒置）。调用方只依赖外观、不接触任何子系统（迪米特法则）；
 * 任一子系统失败均抛出 {@code ServiceException}，由全局异常处理器统一转换为可观察的错误响应。</p>
 *
 * <p>演示入口：{@code POST /pattern/facade/placeOrder}（由后续任务 12.2 的 Demo_Controller 提供）。</p>
 *
 * @since 1.0.0
 */
package com.example.patterns.structural.facade;
