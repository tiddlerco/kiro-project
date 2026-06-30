package com.example.patterns.behavioral.command.command;

import com.example.patterns.behavioral.command.entity.ProductEntity;
import com.example.patterns.behavioral.command.service.ProductService;
import com.example.patterns.common.constant.HttpStatus;
import com.example.patterns.common.exception.ServiceException;

import java.util.Objects;

/**
 * 删除商品命令（命令模式中的「具体命令 ConcreteCommand」角色）。
 *
 * <p>封装一次商品删除操作：{@link #execute()} 先查询并保存删除前的 before 快照，再对商品执行
 * 逻辑删除（状态置 0）；{@link #undo()} 调用接收者恢复（restore）将商品状态置回正常，从而撤销删除。
 * 因删除后数据被逻辑删除，after 快照固定为 {@code null}。</p>
 *
 * <p>本命令为有状态对象（持有接收者引用与目标 id），由调用方按需 {@code new} 创建，
 * 不是 Spring Bean。</p>
 *
 * @since 1.0.0
 */
public class DeleteProductCommand implements OperationCommand {

    /**
     * 接收者：真正执行商品增删改查的服务。
     */
    private final ProductService productService;

    /**
     * 待删除的商品 id。
     */
    private final Long productId;

    /**
     * 执行前的数据快照（删除前的完整商品记录），用于撤销恢复与历史追溯。
     */
    private ProductEntity beforeSnapshot;

    /**
     * 构造删除商品命令。
     *
     * @param productService 接收者商品服务，要求非空
     * @param productId      待删除的商品 id，要求非空
     */
    public DeleteProductCommand(ProductService productService, Long productId) {
        this.productService = Objects.requireNonNull(productService, "商品服务不能为空");
        this.productId = Objects.requireNonNull(productId, "待删除的商品 id 不能为空");
    }

    /**
     * 正向执行：先存 before 快照，再逻辑删除商品。
     *
     * <p>删除前先查询原始记录作为 before 快照；若目标商品不存在则抛出 {@link ServiceException}，
     * 避免删除不存在的数据。</p>
     */
    @Override
    public void execute() {
        this.beforeSnapshot = productService.getProduct(productId);
        if (beforeSnapshot == null) {
            throw new ServiceException(HttpStatus.ERROR, "待删除的商品不存在，id：" + productId);
        }
        productService.deleteProduct(productId);
    }

    /**
     * 逆向撤销：恢复被逻辑删除的商品。
     */
    @Override
    public void undo() {
        productService.restoreProduct(productId);
    }

    /**
     * 返回命令的可读描述。
     *
     * @return 描述本次删除操作的文本
     */
    @Override
    public String describe() {
        return "删除商品[id=" + productId + "]";
    }

    /**
     * 返回命令类型标识。
     *
     * @return 固定为 {@code DELETE}
     */
    @Override
    public String commandType() {
        return "DELETE";
    }

    /**
     * 返回操作目标商品 id。
     *
     * @return 待删除商品的主键 id
     */
    @Override
    public Long targetId() {
        return productId;
    }

    /**
     * 返回执行前的数据快照。
     *
     * @return 删除前的完整商品记录；执行前为 {@code null}
     */
    @Override
    public ProductEntity beforeSnapshot() {
        return beforeSnapshot;
    }

    /**
     * 返回执行后的数据快照。
     *
     * @return 固定为 {@code null}（商品已被逻辑删除）
     */
    @Override
    public ProductEntity afterSnapshot() {
        return null;
    }
}
