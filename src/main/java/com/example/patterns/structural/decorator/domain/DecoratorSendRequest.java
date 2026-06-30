package com.example.patterns.structural.decorator.domain;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 装饰器通知发送请求。
 *
 * <p>承载装饰器模式演示接口 {@code POST /pattern/decorator/send} 的入参：接收人、标题、正文，
 * 以及本次需要叠加的增强能力列表。Controller 借助 {@code @Validated} 对本对象做声明式校验，
 * 接收人、标题、正文均不允许为空。</p>
 *
 * <p>{@link #capabilities} 直接以 {@link NotifyCapability} 枚举列表声明，由框架按枚举常量名
 * （如 {@code SIGNATURE}、{@code ENCRYPT}、{@code LOG}）反序列化，既保证类型安全，又便于服务层
 * 按能力列表逐层组装装饰链。本类为纯数据对象（DTO），不承载任何业务逻辑，getter/setter 由
 * Lombok 的 {@link Data} 注解生成。</p>
 *
 * @since 1.0.0
 */
@Data
public class DecoratorSendRequest {

    /**
     * 接收人标识（如手机号、用户 ID 或邮箱），不允许为空。
     */
    @NotBlank(message = "接收人不能为空")
    private String receiver;

    /**
     * 通知标题，不允许为空。
     */
    @NotBlank(message = "标题不能为空")
    private String title;

    /**
     * 通知正文，不允许为空。
     */
    @NotBlank(message = "正文不能为空")
    private String body;

    /**
     * 需要叠加的增强能力列表（如 SIGNATURE / ENCRYPT / LOG）。
     *
     * <p>按列表顺序由服务层以基础组件为内核逐层包装对应装饰器；为空时表示仅做基础发送、不叠加
     * 任何增强能力。</p>
     */
    private List<NotifyCapability> capabilities;
}
