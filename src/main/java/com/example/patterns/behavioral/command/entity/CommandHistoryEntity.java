package com.example.patterns.behavioral.command.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 命令历史实体（命令模式的可追溯历史）。
 *
 * <p>对应数据库表 {@code sys_command_history}，用于持久化记录每一次被执行的操作命令。
 * 每执行一条命令即落库一行，保存执行前后的数据快照（JSON 字符串）：撤销时读取
 * {@link #beforeSnapshot} 将目标数据恢复至执行前状态，并将该行 {@link #status} 置为 0（已撤销）。</p>
 *
 * <p>本类为纯数据对象（POJO），getter/setter 由 Lombok 的 {@link Data} 注解生成；
 * 数据库列名与属性名遵循下划线转驼峰映射（如 {@code command_type} ↔ {@code commandType}）。</p>
 *
 * @since 1.0.0
 */
@Data
public class CommandHistoryEntity {

    /**
     * 主键 id。
     */
    private Long id;

    /**
     * 命令类型：ADD 新增 / UPDATE 修改 / DELETE 删除。
     */
    private String commandType;

    /**
     * 目标商品 id。
     */
    private Long targetId;

    /**
     * 执行前数据快照（JSON），用于撤销时恢复数据。
     */
    private String beforeSnapshot;

    /**
     * 执行后数据快照（JSON）。
     */
    private String afterSnapshot;

    /**
     * 状态：1 已执行 / 0 已撤销。
     */
    private Integer status;

    /**
     * 操作人。
     */
    private String operator;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;
}
