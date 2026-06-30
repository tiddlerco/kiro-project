package com.example.patterns.behavioral.command.command;

import com.example.patterns.behavioral.command.entity.CommandHistoryEntity;
import com.example.patterns.behavioral.command.entity.ProductEntity;
import com.example.patterns.behavioral.command.mapper.CommandHistoryMapper;
import com.example.patterns.common.constant.HttpStatus;
import com.example.patterns.common.exception.ServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

/**
 * 命令调用者（命令模式中的「调用者 Invoker」角色）。
 *
 * <p>面向 {@link OperationCommand} 接口编程，不关心命令的具体实现：通过 {@link #invoke(OperationCommand, String)}
 * 触发命令执行并把命令历史落库，通过 {@link #undoLast()} 撤销最近一次执行的命令。</p>
 *
 * <p><b>设计取舍——内存命令栈与历史表的分工与对应：</b></p>
 * <ul>
 *     <li><b>内存命令栈</b>（{@link #commandStack}，{@link Deque}）：持有已执行的命令对象本身，
 *         用于支持快速撤销——直接调用命令的 {@link OperationCommand#undo()} 逆向执行，
 *         无需从快照反序列化重建操作逻辑。其生命周期限于当前应用进程（重启后清空）。</li>
 *     <li><b>历史表</b>（{@code sys_command_history}）：持久化每次命令的类型、目标 id、前后快照与状态，
 *         用于跨进程的可追溯与审计。</li>
 *     <li><b>两者的对应关系</b>：每次 {@link #invoke} 先向历史表插入一条 {@code status=1}（已执行）记录，
 *         再将命令压入内存栈；{@link #undoLast} 则先弹出栈顶命令执行撤销，再取历史表中最近一条
 *         {@code status=1} 记录（{@link CommandHistoryMapper#selectLastExecuted()}）标记为已撤销。
 *         由于二者均遵循「后进先出」且插入与压栈同步发生，栈顶命令恒与历史表中「最近一条已执行记录」
 *         相对应，从而保持内存与持久化两侧的一致。</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Service
public class CommandInvoker {

    /**
     * 命令历史持久化 Mapper。
     */
    @Resource
    private CommandHistoryMapper commandHistoryMapper;

    /**
     * JSON 序列化器，用于将前后快照实体序列化为 JSON 字符串存入历史表。
     *
     * <p>注入 Spring 容器中已配置好的 {@link ObjectMapper}（已注册时间类型模块），
     * 以正确序列化实体中的 {@code LocalDateTime} 等字段。</p>
     */
    @Resource
    private ObjectMapper objectMapper;

    /**
     * 内存命令栈，保存已执行且尚未撤销的命令，支持「后进先出」的撤销。
     */
    private final Deque<OperationCommand> commandStack = new ArrayDeque<>();

    /**
     * 执行命令并记录命令历史。
     *
     * <p>流程：先调用命令的 {@link OperationCommand#execute()} 完成实际业务操作；随后将命令类型、
     * 目标 id、前后快照（JSON）、操作人、状态（1 已执行）组装为 {@link CommandHistoryEntity} 落库；
     * 最后将命令压入内存栈以支持后续撤销。</p>
     *
     * @param command  待执行的操作命令，要求非空
     * @param operator 操作人标识，记入命令历史
     */
    public void invoke(OperationCommand command, String operator) {
        Objects.requireNonNull(command, "待执行的命令不能为空");
        command.execute();

        CommandHistoryEntity history = new CommandHistoryEntity();
        history.setCommandType(command.commandType());
        history.setTargetId(command.targetId());
        history.setBeforeSnapshot(toJson(command.beforeSnapshot()));
        history.setAfterSnapshot(toJson(command.afterSnapshot()));
        history.setStatus(1);
        history.setOperator(operator);
        int affected = commandHistoryMapper.insert(history);
        if (affected != 1) {
            throw new ServiceException(HttpStatus.ERROR, "命令历史落库失败");
        }

        commandStack.push(command);
    }

    /**
     * 撤销最近一次执行的命令。
     *
     * <p>流程：弹出内存栈顶命令并调用其 {@link OperationCommand#undo()} 恢复数据；随后取历史表中
     * 最近一条已执行记录并通过 {@link CommandHistoryMapper#markUndone(Long)} 标记为已撤销。
     * 当内存栈为空（无可撤销命令）时抛出 {@link ServiceException}。</p>
     */
    public void undoLast() {
        if (commandStack.isEmpty()) {
            throw new ServiceException(HttpStatus.ERROR, "没有可撤销的命令");
        }
        OperationCommand command = commandStack.pop();
        command.undo();

        CommandHistoryEntity lastExecuted = commandHistoryMapper.selectLastExecuted();
        if (lastExecuted == null) {
            throw new ServiceException(HttpStatus.ERROR, "命令历史缺失，无法标记撤销");
        }
        int affected = commandHistoryMapper.markUndone(lastExecuted.getId());
        if (affected != 1) {
            throw new ServiceException(HttpStatus.ERROR, "命令历史撤销标记失败，id：" + lastExecuted.getId());
        }
    }

    /**
     * 将商品快照实体序列化为 JSON 字符串。
     *
     * <p>快照为 {@code null} 时直接返回 {@code null}（如新增命令的 before、删除命令的 after）；
     * 序列化异常时包装为 {@link ServiceException} 抛出，避免静默失败。</p>
     *
     * @param snapshot 待序列化的商品快照实体，允许为 {@code null}
     * @return 快照对应的 JSON 字符串；入参为 {@code null} 时返回 {@code null}
     */
    private String toJson(ProductEntity snapshot) {
        if (snapshot == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException e) {
            throw new ServiceException(HttpStatus.ERROR, "命令快照序列化失败：" + e.getMessage());
        }
    }
}
