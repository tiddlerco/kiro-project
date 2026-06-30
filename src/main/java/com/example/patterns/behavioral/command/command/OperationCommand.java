package com.example.patterns.behavioral.command.command;

import com.example.patterns.behavioral.command.entity.ProductEntity;

/**
 * 操作命令（命令模式中的「命令 Command」角色）。
 *
 * <p>将一次商品操作（新增 / 修改 / 删除）封装为对象，把「请求的发起」与「请求的执行」解耦：
 * 调用者 {@code CommandInvoker} 只面向本接口编程，无需关心具体操作如何落地。每个具体命令在
 * {@link #execute()} 中调用接收者 {@code ProductService} 完成正向操作，并在 {@link #undo()} 中
 * 执行逆向操作以支持撤销；同时通过 {@link #beforeSnapshot()}/{@link #afterSnapshot()} 暴露执行
 * 前后的数据快照，供调用者统一序列化后写入命令历史，实现可追溯。</p>
 *
 * @since 1.0.0
 */
public interface OperationCommand {

    /**
     * 正向执行本命令。
     *
     * <p>调用接收者完成实际业务操作，并在内部保存执行前（before）与执行后（after）的数据快照、
     * 记录操作目标 id，供调用者落库与后续撤销使用。</p>
     */
    void execute();

    /**
     * 逆向撤销本命令。
     *
     * <p>执行与 {@link #execute()} 相反的操作，将目标数据恢复至命令执行前的状态。
     * 仅应在命令已成功执行后调用。</p>
     */
    void undo();

    /**
     * 返回命令的可读描述。
     *
     * @return 人类可读的命令描述文本，用于日志与演示展示
     */
    String describe();

    /**
     * 返回命令类型标识。
     *
     * @return 命令类型，取值为 {@code ADD}（新增）/ {@code UPDATE}（修改）/ {@code DELETE}（删除）
     */
    String commandType();

    /**
     * 返回操作目标商品 id。
     *
     * <p>对新增命令而言，该值在 {@link #execute()} 完成、主键回填后方可获取。</p>
     *
     * @return 操作目标商品的主键 id；命令尚未执行且无法确定目标时返回 {@code null}
     */
    Long targetId();

    /**
     * 返回执行前的数据快照。
     *
     * @return 命令执行前目标商品的实体快照；新增命令因执行前无数据而返回 {@code null}
     */
    ProductEntity beforeSnapshot();

    /**
     * 返回执行后的数据快照。
     *
     * @return 命令执行后目标商品的实体快照；删除命令因数据已逻辑删除可返回 {@code null}
     */
    ProductEntity afterSnapshot();
}
