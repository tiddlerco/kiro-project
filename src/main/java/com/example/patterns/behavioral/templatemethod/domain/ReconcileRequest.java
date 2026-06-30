package com.example.patterns.behavioral.templatemethod.domain;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 对账请求对象。
 *
 * <p>模板方法模式演示入口 {@code POST /pattern/template/reconcile} 的请求体载体，
 * 承载调用方指定的对账渠道标识。Controller 借助 {@code @Validated} 对本对象进行
 * 声明式参数校验，将「渠道不能为空」的约束从 Controller 方法体中剥离，使控制器
 * 仅保留路由分发职责。</p>
 *
 * <p>本类为纯数据对象（DTO），getter/setter 由 Lombok 的 {@link Data} 注解生成。</p>
 *
 * @since 1.0.0
 */
@Data
public class ReconcileRequest {

    /**
     * 对账渠道标识，如 {@code "alipay"}、{@code "wechat"}。
     */
    @NotBlank(message = "对账渠道标识不能为空")
    private String channel;
}
