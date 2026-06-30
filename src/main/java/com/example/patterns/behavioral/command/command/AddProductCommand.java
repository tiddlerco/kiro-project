package com.example.patterns.behavioral.command.command;

import com.example.patterns.behavioral.command.entity.ProductEntity;
import com.example.patterns.behavioral.command.service.ProductService;

import java.util.Objects;

/**
 * 新增商品命令（命令模式中的「具体命令 ConcreteCommand」角色）。
 *
 * <p>封装一次商品新增操作：{@link #execute()} 调用接收者 {@link ProductService} 新增商品，
 * 并以新增后的完整记录作为 after 快照（before 为空，因执行前无数据）；{@link #undo()} 对
 * 新增的商品执行逻辑删除，从而抵消本次新增。</p>
 *
 * <p>本命令为有状态对象（持有接收者引用与待新增数据），由调用方按需 {@code new} 创建，
 * 不是 Spring Bean。</p>
 *
 * @since 1.0.0
 */
public class AddProductCommand implements OperationCommand {

    /**
     * 接收者：真正执行商品增删改查的服务。
     */
    private final ProductService productService;

    /**
     * 待新增的商品数据（至少包含商品名称与价格）；执行后其 {@code id} 被回填。
     */
    private final ProductEntity product;

    /**
     * 新增成功后的目标商品 id，{@link #execute()} 执行后赋值。
     */
    private Long targetId;

    /**
     * 执行后的数据快照（新增后的完整商品记录）。
     */
    private ProductEntity afterSnapshot;

    /**
     * 构造新增商品命令。
     *
     * @param productService 接收者商品服务，要求非空
     * @param product        待新增的商品数据，要求非空且包含商品名称与价格
     */
    public AddProductCommand(ProductService productService, ProductEntity product) {
        this.productService = Objects.requireNonNull(productService, "商品服务不能为空");
        this.product = Objects.requireNonNull(product, "待新增的商品不能为空");
    }

    /**
     * 正向执行：新增商品。
     *
     * <p>调用接收者新增商品并取得回填的主键 id，随后查询该商品的完整记录作为 after 快照。</p>
     */
    @Override
    public void execute() {
        this.targetId = productService.addProduct(product);
        this.afterSnapshot = productService.getProduct(targetId);
    }

    /**
     * 逆向撤销：逻辑删除本次新增的商品。
     */
    @Override
    public void undo() {
        productService.deleteProduct(targetId);
    }

    /**
     * 返回命令的可读描述。
     *
     * @return 描述本次新增操作的文本
     */
    @Override
    public String describe() {
        return "新增商品[名称=" + product.getProductName() + ", 价格=" + product.getPrice() + "]";
    }

    /**
     * 返回命令类型标识。
     *
     * @return 固定为 {@code ADD}
     */
    @Override
    public String commandType() {
        return "ADD";
    }

    /**
     * 返回操作目标商品 id。
     *
     * @return 新增后回填的商品主键 id；执行前为 {@code null}
     */
    @Override
    public Long targetId() {
        return targetId;
    }

    /**
     * 返回执行前的数据快照。
     *
     * @return 固定为 {@code null}（新增命令执行前无数据）
     */
    @Override
    public ProductEntity beforeSnapshot() {
        return null;
    }

    /**
     * 返回执行后的数据快照。
     *
     * @return 新增后的完整商品记录
     */
    @Override
    public ProductEntity afterSnapshot() {
        return afterSnapshot;
    }
}
