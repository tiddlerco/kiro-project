package com.example.patterns.common.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限校验注解。
 *
 * <p>标注在删除等敏感的演示接口方法上，声明执行该方法所需的权限标识。运行期由
 * {@link PermissionAspect} 环绕拦截：在目标方法执行前读取本注解的权限标识，并与当前登录用户
 * （经 {@link MockUserContext} 获取）的权限集合比对；不具备时抛出
 * {@link com.example.patterns.common.exception.PermissionDeniedException} 阻止方法执行，
 * 从而以“自定义注解 + AOP”替代 Spring Security 完成简化鉴权
 * （满足需求 9.10、9.11，并作为需求 10.2 的 AOP 横切关注点之一）。</p>
 *
 * <p>元注解说明：仅可用于方法（{@link ElementType#METHOD}），并保留至运行期
 * （{@link RetentionPolicy#RUNTIME}），以便切面在调用时通过反射读取所需权限标识。</p>
 *
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresPermission {

    /**
     * 执行被注解方法所需的权限标识。
     *
     * @return 权限标识字符串，形如 {@code pattern:product:remove}
     */
    String value();
}
