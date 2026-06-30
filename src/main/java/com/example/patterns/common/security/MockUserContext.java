package com.example.patterns.common.security;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 模拟登录用户上下文。
 *
 * <p>在不引入 Spring Security 的前提下，为简化权限校验机制提供“当前登录用户”的来源，
 * 以便演示权限校验“通过”与“拒绝”两条路径。当前用户的解析遵循以下优先级：</p>
 * <ol>
 *     <li><b>线程级显式指定</b>：通过 {@link #setCurrentUser(LoginUser)} 设置（主要用于单元测试或
 *         非 Web 场景），优先级最高；使用后应调用 {@link #clear()} 清理，避免线程池复用导致用户态泄漏。</li>
 *     <li><b>请求头切换</b>：从当前 HTTP 请求头 {@value #MOCK_USER_HEADER} 读取用户名，命中预置模拟用户表
 *         即返回对应用户。例如演示拒绝路径时携带 {@code X-Mock-User: guest}（无任何权限的访客）。</li>
 *     <li><b>默认用户</b>：以上均未命中时返回默认用户 {@value #DEFAULT_USERNAME}（拥有全部权限的管理员），
 *         使绝大多数演示接口在不额外指定时默认放行。</li>
 * </ol>
 *
 * <p>预置两个模拟用户：{@code admin}（持有全部权限通配符 {@link LoginUser#ALL_PERMISSION}）与
 * {@code guest}（权限集合为空），分别用于演示权限校验的通过与拒绝。</p>
 *
 * @since 1.0.0
 */
@Component
public class MockUserContext {

    /** 用于切换模拟用户的请求头名称。 */
    public static final String MOCK_USER_HEADER = "X-Mock-User";

    /** 默认模拟用户名：拥有全部权限的管理员，未显式切换时使用。 */
    public static final String DEFAULT_USERNAME = "admin";

    /** 访客模拟用户名：不具备任何权限，用于演示权限校验拒绝路径。 */
    public static final String GUEST_USERNAME = "guest";

    /** 预置模拟用户表：用户名 -> 登录用户，初始化后作为只读字典使用。 */
    private final Map<String, LoginUser> mockUsers = buildMockUsers();

    /** 线程级当前用户覆盖，用于单元测试或非 Web 场景显式指定当前操作者。 */
    private final ThreadLocal<LoginUser> currentUserHolder = new ThreadLocal<>();

    /**
     * 获取当前登录用户。
     *
     * <p>按“线程级显式指定 → 请求头切换 → 默认管理员”的优先级解析，保证在 Web 演示、单元测试与
     * 非 Web 场景下均能返回一个非空的当前用户。</p>
     *
     * @return 当前登录用户，恒不为 {@code null}
     */
    public LoginUser getCurrentUser() {
        LoginUser overrideUser = currentUserHolder.get();
        if (overrideUser != null) {
            return overrideUser;
        }
        String mockUsername = resolveUsernameFromRequestHeader();
        if (StringUtils.hasText(mockUsername) && mockUsers.containsKey(mockUsername)) {
            return mockUsers.get(mockUsername);
        }
        return mockUsers.get(DEFAULT_USERNAME);
    }

    /**
     * 显式设置当前线程的登录用户。
     *
     * <p>主要用于单元测试或非 Web 场景下精确控制当前操作者；设置后优先级高于请求头与默认用户。
     * 使用完毕应调用 {@link #clear()} 清理，以免线程被复用时残留用户态。</p>
     *
     * @param loginUser 待设置为当前操作者的登录用户
     */
    public void setCurrentUser(LoginUser loginUser) {
        currentUserHolder.set(loginUser);
    }

    /**
     * 清除当前线程显式设置的登录用户。
     *
     * <p>用于释放 {@link #setCurrentUser(LoginUser)} 写入的线程级用户，避免线程池复用导致的用户态泄漏。</p>
     */
    public void clear() {
        currentUserHolder.remove();
    }

    /**
     * 从当前 HTTP 请求头解析模拟用户名。
     *
     * <p>当不处于 Web 请求上下文（如单元测试、启动监听）时安全返回 {@code null}，由上层回退到默认用户。</p>
     *
     * @return 请求头 {@value #MOCK_USER_HEADER} 中携带的用户名；无 Web 上下文或未携带时返回 {@code null}
     */
    private String resolveUsernameFromRequestHeader() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (!(requestAttributes instanceof ServletRequestAttributes)) {
            return null;
        }
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        return request.getHeader(MOCK_USER_HEADER);
    }

    /**
     * 构建预置模拟用户表。
     *
     * <p>初始化管理员（拥有全部权限通配符）与访客（无任何权限）两个用户，分别支撑权限校验的通过与拒绝演示；
     * 二者的权限集合均采用不可变集合，避免预置用户的权限在演示过程中被误改而相互影响。</p>
     *
     * @return 用户名到登录用户的映射
     */
    private Map<String, LoginUser> buildMockUsers() {
        Map<String, LoginUser> users = new HashMap<>(4);
        Set<String> adminPermissions = Collections.singleton(LoginUser.ALL_PERMISSION);
        Set<String> guestPermissions = Collections.emptySet();
        users.put(DEFAULT_USERNAME, new LoginUser(1L, DEFAULT_USERNAME, adminPermissions));
        users.put(GUEST_USERNAME, new LoginUser(2L, GUEST_USERNAME, guestPermissions));
        return users;
    }
}
