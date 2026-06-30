package com.example.patterns.common.core.domain;

import com.example.patterns.common.constant.HttpStatus;
import lombok.Data;

/**
 * 统一响应结果。
 *
 * <p>承载单对象或无数据类接口的统一返回结构，由 {@code code}（响应状态码）、
 * {@code msg}（响应消息）、{@code data}（响应数据）三个独立字段组成。
 * 相较于继承 {@code HashMap} 的写法，独立字段实现更具类型安全性。</p>
 *
 * <p>统一通过一组静态工厂方法构造成功或失败的响应，避免调用方手工拼装字段。
 * getter/setter 由 Lombok 的 {@link Data} 注解生成。</p>
 *
 * @since 1.0.0
 */
@Data
public class AjaxResult {

    /**
     * 响应状态码。
     */
    private int code;

    /**
     * 响应消息。
     */
    private String msg;

    /**
     * 响应数据。
     */
    private Object data;

    /**
     * 私有全参构造方法。
     *
     * <p>统一通过静态工厂方法创建实例，禁止外部直接 new。</p>
     *
     * @param code 响应状态码
     * @param msg  响应消息
     * @param data 响应数据
     */
    private AjaxResult(int code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 返回成功结果（无数据）。
     *
     * @return 表示操作成功的统一响应结果
     */
    public static AjaxResult success() {
        return new AjaxResult(HttpStatus.SUCCESS, "操作成功", null);
    }

    /**
     * 返回成功结果（携带数据）。
     *
     * @param data 需返回给调用方的业务数据
     * @return 携带业务数据且表示操作成功的统一响应结果
     */
    public static AjaxResult success(Object data) {
        return new AjaxResult(HttpStatus.SUCCESS, "操作成功", data);
    }

    /**
     * 返回成功结果（自定义消息与数据）。
     *
     * @param msg  自定义成功提示消息
     * @param data 需返回给调用方的业务数据
     * @return 携带自定义消息与业务数据且表示操作成功的统一响应结果
     */
    public static AjaxResult success(String msg, Object data) {
        return new AjaxResult(HttpStatus.SUCCESS, msg, data);
    }

    /**
     * 返回失败结果（默认消息）。
     *
     * @return 表示操作失败的统一响应结果
     */
    public static AjaxResult error() {
        return new AjaxResult(HttpStatus.ERROR, "操作失败", null);
    }

    /**
     * 返回失败结果（自定义消息）。
     *
     * @param msg 自定义失败提示消息
     * @return 携带自定义消息且表示操作失败的统一响应结果
     */
    public static AjaxResult error(String msg) {
        return new AjaxResult(HttpStatus.ERROR, msg, null);
    }

    /**
     * 返回失败结果（自定义状态码与消息）。
     *
     * @param code 自定义响应状态码
     * @param msg  自定义失败提示消息
     * @return 携带自定义状态码与消息且表示操作失败的统一响应结果
     */
    public static AjaxResult error(int code, String msg) {
        return new AjaxResult(code, msg, null);
    }
}
