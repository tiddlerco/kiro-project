package com.example.patterns.behavioral.command.mapper;

import com.example.patterns.behavioral.command.entity.CommandHistoryEntity;
import org.apache.ibatis.annotations.Param;

/**
 * 命令历史 Mapper 接口（命令模式可追溯历史的持久化访问）。
 *
 * <p>面向 {@code sys_command_history} 表提供命令历史的写入与撤销支持：命令执行时落库一条历史记录；
 * 撤销时取出最近一条「已执行」的记录，并将其标记为「已撤销」。所有 SQL 语句均外置于
 * {@code mapper/command/CommandHistoryMapper.xml}，本接口不使用任何
 * MyBatis-Plus Wrapper（QueryWrapper / LambdaQueryWrapper）。</p>
 *
 * @since 1.0.0
 */
public interface CommandHistoryMapper {

    /**
     * 新增一条命令历史记录。
     *
     * <p>插入成功后，由数据库自增主键回填至入参对象的 {@code id} 属性
     * （在 XML 中通过 {@code useGeneratedKeys} 与 {@code keyProperty} 实现）。</p>
     *
     * @param history 待新增的命令历史实体
     * @return 受影响的行数（新增成功为 1）
     */
    int insert(CommandHistoryEntity history);

    /**
     * 查询最近一条「已执行」（status=1）的命令历史。
     *
     * <p>按主键 id 倒序取首条，供撤销操作定位最近一次尚未撤销的命令。</p>
     *
     * @return 最近一条已执行的命令历史；不存在时返回 {@code null}
     */
    CommandHistoryEntity selectLastExecuted();

    /**
     * 将指定命令历史标记为「已撤销」。
     *
     * <p>将该行状态由 1（已执行）置为 0（已撤销），表示对应命令已被撤销。</p>
     *
     * @param id 目标命令历史记录的主键 id
     * @return 受影响的行数（标记成功为 1）
     */
    int markUndone(@Param("id") Long id);
}
