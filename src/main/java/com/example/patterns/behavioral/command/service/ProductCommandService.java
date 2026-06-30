package com.example.patterns.behavioral.command.service;

import com.example.patterns.behavioral.command.command.AddProductCommand;
import com.example.patterns.behavioral.command.command.CommandInvoker;
import com.example.patterns.behavioral.command.command.DeleteProductCommand;
import com.example.patterns.behavioral.command.command.OperationCommand;
import com.example.patterns.behavioral.command.command.UpdateProductCommand;
import com.example.patterns.behavioral.command.domain.CommandExecuteRequest;
import com.example.patterns.behavioral.command.entity.ProductEntity;
import com.example.patterns.common.constant.HttpStatus;
import com.example.patterns.common.exception.ServiceException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 命令编排服务。
 *
 * <p>命令模式演示中「请求发起方」与「调用者 {@link CommandInvoker}」之间的编排层：负责根据请求
 * 构造对应的具体命令（{@link AddProductCommand} / {@link UpdateProductCommand} /
 * {@link DeleteProductCommand}）并交由调用者执行或撤销，从而使控制器只需路由分发、不接触命令的
 * 构造与编排细节（与项目「Controller 仅路由分发，业务逻辑下沉到 Service」的规范一致）。</p>
 *
 * <p>本服务持有接收者 {@link ProductService} 用于构造命令、持有调用者 {@link CommandInvoker} 用于
 * 触发命令执行与历史落库；命令对象为有状态对象，按需 {@code new} 创建而非纳入容器。</p>
 *
 * @since 1.0.0
 */
@Service
public class ProductCommandService {

    /**
     * 命令调用者：触发命令执行并记录历史、支持撤销。
     */
    @Resource
    private CommandInvoker commandInvoker;

    /**
     * 接收者商品服务：构造各具体命令时作为命令的接收者注入。
     */
    @Resource
    private ProductService productService;

    /**
     * 按命令类型执行命令。
     *
     * <p>根据请求中的命令类型构造对应的具体命令，并委派调用者执行与落库，使控制器保持仅路由分发的职责。</p>
     *
     * @param request 命令执行请求，含命令类型、操作人及随类型按需填写的商品字段，要求非空
     */
    public void execute(CommandExecuteRequest request) {
        OperationCommand command = buildCommand(request);
        commandInvoker.invoke(command, request.getOperator());
    }

    /**
     * 撤销最近一次执行的命令。
     *
     * <p>委派调用者弹出内存命令栈顶命令执行逆向操作，并将命令历史标记为已撤销。</p>
     */
    public void undoLast() {
        commandInvoker.undoLast();
    }

    /**
     * 执行删除命令（删除接口 {@code GET /pattern/command/deleteProduct} 专用）。
     *
     * <p>在构造删除命令前，先校验「删除确认标识」是否为真：仅当 {@code confirmDelete} 显式为
     * {@code true} 时才构造 {@link DeleteProductCommand} 并交由调用者执行；否则视为未确认，
     * 抛出 {@link ServiceException} 拒绝删除，目标数据保持不变（落实需求 9.10、9.11 中「确认标识为真」
     * 一侧的安全校验，权限校验由控制器方法上的注解与权限切面完成）。</p>
     *
     * @param productId     待删除的商品 id，要求非空（其非空由控制器的必填参数保证）
     * @param confirmDelete 删除确认标识，仅当为 {@link Boolean#TRUE} 时才放行删除
     * @param operator      操作人标识，记入命令历史
     */
    public void executeDelete(Long productId, Boolean confirmDelete, String operator) {
        if (!Boolean.TRUE.equals(confirmDelete)) {
            throw new ServiceException(HttpStatus.ERROR, "删除确认标识缺失或非真，已拒绝删除操作，目标数据保持不变");
        }
        DeleteProductCommand command = new DeleteProductCommand(productService, productId);
        commandInvoker.invoke(command, operator);
    }

    /**
     * 根据命令类型构造对应的具体命令。
     *
     * <p>按请求的命令类型分发：{@code ADD} 构造新增命令、{@code UPDATE} 构造修改命令、
     * {@code DELETE} 构造删除命令；命令类型不被支持时抛出 {@link ServiceException}。</p>
     *
     * @param request 命令执行请求，要求非空
     * @return 与命令类型匹配的具体命令对象
     */
    private OperationCommand buildCommand(CommandExecuteRequest request) {
        String commandType = request.getCommandType();
        if ("ADD".equalsIgnoreCase(commandType)) {
            return buildAddCommand(request);
        }
        if ("UPDATE".equalsIgnoreCase(commandType)) {
            return buildUpdateCommand(request);
        }
        if ("DELETE".equalsIgnoreCase(commandType)) {
            return buildDeleteCommand(request);
        }
        throw new ServiceException(HttpStatus.ERROR, "不支持的命令类型：" + commandType);
    }

    /**
     * 构造新增商品命令。
     *
     * <p>以请求中的商品名称与价格组装待新增商品实体，并以接收者商品服务构造 {@link AddProductCommand}。</p>
     *
     * @param request 命令执行请求，要求其商品名称与价格非空
     * @return 新增商品命令
     */
    private OperationCommand buildAddCommand(CommandExecuteRequest request) {
        ProductEntity product = new ProductEntity();
        product.setProductName(request.getProductName());
        product.setPrice(request.getPrice());
        return new AddProductCommand(productService, product);
    }

    /**
     * 构造修改商品命令。
     *
     * <p>以请求中的主键 id 定位记录，并以商品名称、价格作为新值组装待更新商品实体，
     * 进而以接收者商品服务构造 {@link UpdateProductCommand}。</p>
     *
     * @param request 命令执行请求，要求其主键 id 非空
     * @return 修改商品命令
     */
    private OperationCommand buildUpdateCommand(CommandExecuteRequest request) {
        ProductEntity newData = new ProductEntity();
        newData.setId(request.getId());
        newData.setProductName(request.getProductName());
        newData.setPrice(request.getPrice());
        return new UpdateProductCommand(productService, newData);
    }

    /**
     * 构造删除商品命令。
     *
     * <p>以请求中的主键 id 与接收者商品服务构造 {@link DeleteProductCommand}。</p>
     *
     * @param request 命令执行请求，要求其主键 id 非空
     * @return 删除商品命令
     */
    private OperationCommand buildDeleteCommand(CommandExecuteRequest request) {
        return new DeleteProductCommand(productService, request.getId());
    }
}
