package com.example.patterns.behavioral.command.command;

import com.example.patterns.behavioral.command.entity.ProductEntity;
import com.example.patterns.behavioral.command.service.ProductService;
import com.example.patterns.common.constant.HttpStatus;
import com.example.patterns.common.exception.ServiceException;

import java.util.Objects;

/**
 * 修改商品命令（命令模式中的「具体命令 ConcreteCommand」角色）。
 *
 * <p>封装一次商品修改操作：{@link #execute()} 先查询并保存修改前的 before 快照，再按 id 更新，
 * 随后查询更新后的记录作为 after 快照；{@link #undo()} 用 before 快照将商品还原回修改前的字段值。</p>
 *
 * <p>本命令为有状态对象（持有接收者引用与待更新数据），由调用方按需 {@code new} 创建，
 * 不是 Spring Bean。</p>
 *
 * @since 1.0.0
 */
public class UpdateProductCommand implements OperationCommand {

    /**
     * 接收者：真正执行商品增删改查的服务。
     */
    private final ProductService productService;

    /**
     * 待更新的商品数据（必须携带主键 id 及待更新字段）。
     */
    private final ProductEntity newData;

    /**
     * 执行前的数据快照（更新前的完整商品记录），用于撤销还原。
     */
    private ProductEntity beforeSnapshot;

    /**
     * 执行后的数据快照（更新后的完整商品记录）。
     */
    private ProductEntity afterSnapshot;

    /**
     * 构造修改商品命令。
     *
     * @param productService 接收者商品服务，要求非空
     * @param newData        待更新的商品数据，要求非空且 {@code id} 不为空
     */
    public UpdateProductCommand(ProductService productService, ProductEntity newData) {
        this.productService = Objects.requireNonNull(productService, "商品服务不能为空");
        this.newData = Objects.requireNonNull(newData, "待更新的商品不能为空");
        Objects.requireNonNull(newData.getId(), "待更新的商品 id 不能为空");
    }

    /**
     * 正向执行：先存 before 快照，再更新，最后取 after 快照。
     *
     * <p>更新前先查询原始记录作为 before 快照；若目标商品不存在则抛出 {@link ServiceException}，
     * 避免在无原始数据可还原的情况下执行更新。</p>
     */
    @Override
    public void execute() {
        this.beforeSnapshot = productService.getProduct(newData.getId());
        if (beforeSnapshot == null) {
            throw new ServiceException(HttpStatus.ERROR, "待更新的商品不存在，id：" + newData.getId());
        }
        productService.updateProduct(newData);
        this.afterSnapshot = productService.getProduct(newData.getId());
    }

    /**
     * 逆向撤销：用 before 快照将商品字段还原回更新前的值。
     */
    @Override
    public void undo() {
        productService.updateProduct(beforeSnapshot);
    }

    /**
     * 返回命令的可读描述。
     *
     * @return 描述本次修改操作的文本
     */
    @Override
    public String describe() {
        return "修改商品[id=" + newData.getId() + "]";
    }

    /**
     * 返回命令类型标识。
     *
     * @return 固定为 {@code UPDATE}
     */
    @Override
    public String commandType() {
        return "UPDATE";
    }

    /**
     * 返回操作目标商品 id。
     *
     * @return 待更新商品的主键 id
     */
    @Override
    public Long targetId() {
        return newData.getId();
    }

    /**
     * 返回执行前的数据快照。
     *
     * @return 更新前的完整商品记录；执行前为 {@code null}
     */
    @Override
    public ProductEntity beforeSnapshot() {
        return beforeSnapshot;
    }

    /**
     * 返回执行后的数据快照。
     *
     * @return 更新后的完整商品记录；执行前为 {@code null}
     */
    @Override
    public ProductEntity afterSnapshot() {
        return afterSnapshot;
    }
}
