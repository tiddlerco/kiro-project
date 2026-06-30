package com.example.patterns.creational.singleton.domain;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 全局配置设置请求。
 *
 * <p>承载单例模式演示入口「设置全局配置」所需的输入参数，作为 Demo_Controller 的请求对象，
 * 经 {@code @Validated} 触发声明式校验，避免在控制器内手写 if 校验逻辑。</p>
 *
 * <p>本类为纯数据对象（DTO），不承载业务逻辑，使用 Lombok {@code @Data} 生成
 * getter/setter/equals/hashCode/toString。</p>
 *
 * @since 1.0.0
 */
@Data
public class SingletonConfigRequest {

    /** 配置键，不能为空 */
    @NotBlank(message = "配置键不能为空")
    private String key;

    /** 配置值，不能为空 */
    @NotBlank(message = "配置值不能为空")
    private String value;
}
