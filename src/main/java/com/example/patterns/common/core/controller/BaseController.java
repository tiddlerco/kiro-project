package com.example.patterns.common.core.controller;

import com.example.patterns.common.core.domain.AjaxResult;
import com.example.patterns.common.core.domain.TableDataInfo;

import java.util.List;

/**
 * 控制器基类。
 *
 * <p>为各设计模式的 Demo_Controller 提供统一的返回结果与分页封装方法，
 * 使 Controller 层仅负责路由分发（接收参数 → 调用 Service → 返回结果），
 * 避免在各控制器中重复手工拼装 {@link AjaxResult} 与 {@link TableDataInfo}。</p>
 *
 * <p>所有封装方法均以 {@code protected} 暴露，仅供子类（各 Demo_Controller）继承调用，
 * 底层统一复用 {@link AjaxResult} 与 {@link TableDataInfo} 的静态工厂方法，保证全工程
 * 响应结构与状态码语义一致。</p>
 *
 * @since 1.0.0
 */
public class BaseController {

    /**
     * 返回成功结果（无数据）。
     *
     * <p>适用于新增、修改等无需向调用方回传业务数据、仅需告知操作成功的场景。</p>
     *
     * @return 表示操作成功的统一响应结果
     */
    protected AjaxResult success() {
        return AjaxResult.success();
    }

    /**
     * 返回成功结果（携带数据）。
     *
     * <p>适用于查询单对象等需要向调用方回传业务数据的场景。</p>
     *
     * @param data 需返回给调用方的业务数据
     * @return 携带业务数据且表示操作成功的统一响应结果
     */
    protected AjaxResult success(Object data) {
        return AjaxResult.success(data);
    }

    /**
     * 返回失败结果（自定义消息）。
     *
     * <p>适用于业务校验未通过等需要向调用方返回明确失败原因的场景。</p>
     *
     * @param msg 自定义失败提示消息
     * @return 携带自定义消息且表示操作失败的统一响应结果
     */
    protected AjaxResult error(String msg) {
        return AjaxResult.error(msg);
    }

    /**
     * 按受影响行数返回增删改结果。
     *
     * <p>用于将增删改操作的受影响行数转换为统一响应：受影响行数大于 0 视为操作成功，
     * 否则视为操作失败。</p>
     *
     * @param rows 增删改操作影响的数据库行数
     * @return 受影响行数大于 0 时返回成功响应，否则返回失败响应
     */
    protected AjaxResult toAjax(int rows) {
        return rows > 0 ? AjaxResult.success() : AjaxResult.error();
    }

    /**
     * 将列表封装为统一分页/列表返回结构。
     *
     * <p>通过泛型参数 {@code T} 约束列表元素类型并透传至 {@link TableDataInfo}，
     * 保证列表/分页返回的类型安全，避免调用方进行强制类型转换。</p>
     *
     * @param list 待封装的列表数据
     * @param <T>  列表中元素的类型
     * @return 封装了列表数据、总记录数与成功状态的统一分页返回结果
     */
    protected <T> TableDataInfo<T> getDataTable(List<T> list) {
        return TableDataInfo.build(list);
    }
}
