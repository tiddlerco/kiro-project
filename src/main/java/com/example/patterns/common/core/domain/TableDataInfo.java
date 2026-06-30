package com.example.patterns.common.core.domain;

import com.example.patterns.common.constant.HttpStatus;
import lombok.Data;

import java.util.List;

/**
 * 统一分页/列表返回结果。
 *
 * <p>承载列表或分页类接口的统一返回结构，由 {@code total}（总记录数）、
 * {@code rows}（列表数据）、{@code code}（响应状态码）、{@code msg}（响应消息）
 * 四个字段组成。通过泛型参数 {@code T} 约束列表元素类型，保证列表返回的类型安全。</p>
 *
 * <p>getter/setter 由 Lombok 的 {@link Data} 注解生成，构造统一通过静态工厂方法
 * {@link #build(List)} 完成。</p>
 *
 * @param <T> 列表中元素的类型
 * @since 1.0.0
 */
@Data
public class TableDataInfo<T> {

    /**
     * 总记录数。
     */
    private long total;

    /**
     * 列表数据。
     */
    private List<T> rows;

    /**
     * 响应状态码。
     */
    private int code;

    /**
     * 响应消息。
     */
    private String msg;

    /**
     * 默认构造方法。
     *
     * <p>保留无参构造以兼容序列化框架，业务代码统一通过 {@link #build(List)} 构造。</p>
     */
    public TableDataInfo() {
    }

    /**
     * 构造分页/列表返回结果（成功）。
     *
     * <p>以传入列表作为返回数据，并将总记录数置为列表元素个数；
     * 当列表为 {@code null} 时，总记录数记为 0。</p>
     *
     * @param rows 列表数据
     * @param <T>  列表中元素的类型
     * @return 封装了列表数据、总记录数与成功状态的统一分页返回结果
     */
    public static <T> TableDataInfo<T> build(List<T> rows) {
        TableDataInfo<T> dataInfo = new TableDataInfo<>();
        dataInfo.setRows(rows);
        dataInfo.setTotal(rows == null ? 0L : rows.size());
        dataInfo.setCode(HttpStatus.SUCCESS);
        dataInfo.setMsg("查询成功");
        return dataInfo;
    }
}
