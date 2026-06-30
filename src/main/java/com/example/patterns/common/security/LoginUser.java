package com.example.patterns.common.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Set;

/**
 * 当前登录用户模型。
 *
 * <p>承载简化权限校验机制中“当前操作者”的身份信息与其拥有的权限标识集合，由
 * {@link MockUserContext} 负责创建与切换，并由 {@link PermissionAspect} 在拦截
 * {@link RequiresPermission} 标注方法时读取以判定是否放行（服务于需求 9.10、9.11、10.2）。</p>
 *
 * <p>设计说明：将“是否覆盖某权限”的判定逻辑（含全部权限通配符处理）内聚于本模型的
 * {@link #hasPermission(String)} 方法，使切面只需委派调用而无需了解权限集合的内部结构，
 * 既遵循单一职责原则，也避免调用方对权限集合产生链式穿透访问（迪米特法则）。</p>
 *
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser {

    /**
     * 全部权限通配符标识。
     *
     * <p>当用户的权限集合包含该标识时，视为拥有任意权限（管理员语义），借鉴 RuoYi 的
     * {@code *:*:*} 约定，便于演示“有权用户”一键放行的通过路径。</p>
     */
    public static final String ALL_PERMISSION = "*:*:*";

    /** 用户唯一标识。 */
    private Long userId;

    /** 用户名（登录账号）。 */
    private String username;

    /** 用户拥有的权限标识集合，元素形如 {@code pattern:product:remove}。 */
    private Set<String> permissions;

    /**
     * 判断当前用户是否具备指定的权限标识。
     *
     * <p>判定规则依次为：所需权限为空白字符串、或用户无任何权限时一律视为不具备；用户持有全部权限
     * 通配符 {@link #ALL_PERMISSION} 时具备任意权限；否则当且仅当权限集合精确包含所需权限标识时具备。</p>
     *
     * @param requiredPermission 目标方法所需的权限标识
     * @return 具备该权限返回 {@code true}，否则返回 {@code false}
     */
    public boolean hasPermission(String requiredPermission) {
        if (!StringUtils.hasText(requiredPermission)) {
            return false;
        }
        if (CollectionUtils.isEmpty(permissions)) {
            return false;
        }
        if (permissions.contains(ALL_PERMISSION)) {
            return true;
        }
        return permissions.contains(requiredPermission);
    }
}
