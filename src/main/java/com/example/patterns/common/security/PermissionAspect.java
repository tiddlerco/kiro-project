package com.example.patterns.common.security;

import com.example.patterns.common.exception.PermissionDeniedException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 权限校验切面。
 *
 * <p>以 Spring AOP 环绕通知拦截所有标注了 {@link RequiresPermission} 的方法：在目标方法执行前，
 * 读取注解声明的所需权限标识，并与当前登录用户（经 {@link MockUserContext} 获取）的权限集合比对；
 * 不具备时抛出 {@link PermissionDeniedException} 阻止目标方法执行，目标数据保持不变，
 * 该异常随后由全局异常处理器转换为可观察的错误响应（满足需求 9.10、9.11、11.3）。具备权限时放行，
 * 调用目标方法并原样返回其结果。</p>
 *
 * <p>本切面与代理模式的缓存限流切面共同构成需求 10.2 要求的 Spring AOP 横切关注点示例（此处为权限校验）。
 * 切面自身不感知具体的权限集合结构，鉴权决策中“用户是否拥有某权限”的判定委派给 {@link LoginUser#hasPermission(String)}，
 * 保持职责单一与低耦合。</p>
 *
 * @since 1.0.0
 */
@Aspect
@Component
public class PermissionAspect {

    /** 模拟登录用户上下文，用于获取当前登录用户及其权限集合。 */
    @Resource
    private MockUserContext mockUserContext;

    /**
     * 权限校验环绕通知：校验当前用户是否具备注解声明的权限。
     *
     * <p>切点表达式 {@code @annotation(requiresPermission)} 将目标方法上的 {@link RequiresPermission}
     * 注解实例绑定到同名参数。校验通过则调用 {@link ProceedingJoinPoint#proceed()} 放行；否则抛出
     * {@link PermissionDeniedException}，不再执行目标方法。</p>
     *
     * @param point              连接点，代表被拦截的目标方法调用
     * @param requiresPermission 目标方法上的权限注解，其 {@code value} 为所需权限标识
     * @return 目标方法的执行结果
     * @throws Throwable 目标方法执行过程中抛出的任意异常将向上传播
     */
    @Around("@annotation(requiresPermission)")
    public Object around(ProceedingJoinPoint point, RequiresPermission requiresPermission) throws Throwable {
        String requiredPermission = requiresPermission.value();
        if (!hasPermission(requiredPermission)) {
            throw new PermissionDeniedException("无操作权限，缺少权限标识：" + requiredPermission);
        }
        return point.proceed();
    }

    /**
     * 判断当前登录用户是否具备指定的权限标识。
     *
     * <p>先经 {@link MockUserContext} 解析当前操作者，再委派 {@link LoginUser#hasPermission(String)} 完成
     * 权限覆盖判定，避免在切面内对权限集合做链式穿透访问（迪米特法则）。</p>
     *
     * @param requiredPermission 目标方法所需的权限标识
     * @return 当前用户具备该权限返回 {@code true}，否则返回 {@code false}
     */
    private boolean hasPermission(String requiredPermission) {
        LoginUser currentUser = mockUserContext.getCurrentUser();
        return currentUser.hasPermission(requiredPermission);
    }
}
