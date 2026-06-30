package com.example.patterns.behavioral.command.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体（命令模式的操作目标）。
 *
 * <p>对应数据库表 {@code biz_product}，是命令模式中各操作命令（新增 / 修改 / 删除）
 * 真正作用的目标数据对象。该表采用逻辑删除（将 {@link #status} 置为 0）而非物理删除，
 * 从而支持删除命令的撤销恢复。</p>
 *
 * <p>本类为纯数据对象（POJO），getter/setter 由 Lombok 的 {@link Data} 注解生成；
 * 数据库列名与属性名遵循下划线转驼峰映射（如 {@code product_name} ↔ {@code productName}）。</p>
 *
 * @since 1.0.0
 */
@Data
public class ProductEntity {

    /**
     * 主键 id。
     */
    private Long id;

    /**
     * 商品名称。
     */
    private String productName;

    /**
     * 商品价格（单位：元）。
     */
    private BigDecimal price;

    /**
     * 状态：1 正常 / 0 已删除（逻辑删除，便于命令撤销恢复）。
     */
    private Integer status;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;
}
