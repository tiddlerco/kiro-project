package com.example.patterns.behavioral.command.domain;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

/**
 * 命令执行请求。
 *
 * <p>命令模式演示接口 {@code POST /pattern/command/execute} 的入参对象，承载一次命令编排所需的
 * 全部输入：命令类型、操作人，以及随命令类型按需填写的商品字段。其上的校验注解供控制器以
 * {@code @Validated} 触发声明式参数校验，避免在控制器中手写 if 校验逻辑（与项目统一的请求对象
 * 校验规范一致）。</p>
 *
 * <p>本类为纯数据对象（DTO），不承载任何命令构造或编排逻辑：根据 {@link #commandType} 构造对应
 * 命令并执行的职责由 Service 层负责，使控制器保持仅路由分发的职责。getter/setter 由 Lombok 的
 * {@link Data} 注解生成；商品价格采用 {@link BigDecimal} 表示，避免浮点类型在金额场景引入精度误差。</p>
 *
 * <p>字段与命令类型的对应关系：</p>
 * <ul>
 *     <li>{@code ADD}（新增）：使用 {@link #productName} 与 {@link #price}，无需 {@link #id}。</li>
 *     <li>{@code UPDATE}（修改）：使用 {@link #id} 定位记录，并以 {@link #productName}、{@link #price} 作为新值。</li>
 *     <li>{@code DELETE}（删除）：使用 {@link #id} 定位待删除记录。</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Data
public class CommandExecuteRequest {

    /**
     * 命令类型。
     *
     * <p>取值为 {@code ADD}（新增）/ {@code UPDATE}（修改）/ {@code DELETE}（删除），必填，
     * Service 据此构造对应的具体命令。</p>
     */
    @NotBlank(message = "命令类型不能为空")
    private String commandType;

    /**
     * 操作人标识。
     *
     * <p>随命令一并记入命令历史，用于操作可追溯，必填。</p>
     */
    @NotBlank(message = "操作人不能为空")
    private String operator;

    /**
     * 商品主键 id。
     *
     * <p>{@code UPDATE}、{@code DELETE} 命令用于定位目标记录；{@code ADD} 命令无需填写，可为空。</p>
     */
    private Long id;

    /**
     * 商品名称。
     *
     * <p>{@code ADD}、{@code UPDATE} 命令使用；{@code DELETE} 命令无需填写，可为空。</p>
     */
    private String productName;

    /**
     * 商品价格（单位：元）。
     *
     * <p>{@code ADD}、{@code UPDATE} 命令使用；{@code DELETE} 命令无需填写，可为空。</p>
     */
    private BigDecimal price;
}
