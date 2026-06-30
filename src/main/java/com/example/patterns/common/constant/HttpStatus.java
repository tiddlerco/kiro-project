package com.example.patterns.common.constant;

/**
 * 响应状态码常量。
 *
 * <p>集中定义统一响应中使用的 HTTP 风格状态码，供 {@link com.example.patterns.common.core.domain.AjaxResult}、
 * {@link com.example.patterns.common.core.domain.TableDataInfo} 等统一返回结构及全局异常处理器复用，
 * 避免在各处硬编码魔法数字，保证响应码语义一致。</p>
 *
 * @since 1.0.0
 */
public class HttpStatus {

    /**
     * 操作成功。
     */
    public static final int SUCCESS = 200;

    /**
     * 请求参数错误（客户端请求不合法）。
     */
    public static final int BAD_REQUEST = 400;

    /**
     * 未授权（未登录或登录态已失效）。
     */
    public static final int UNAUTHORIZED = 401;

    /**
     * 访问受限（已登录但权限不足）。
     */
    public static final int FORBIDDEN = 403;

    /**
     * 资源不存在。
     */
    public static final int NOT_FOUND = 404;

    /**
     * 系统内部错误。
     */
    public static final int ERROR = 500;

    /**
     * 私有构造方法。
     *
     * <p>本类为纯常量类，不应被实例化，故将构造方法私有化以禁止外部 new。</p>
     */
    private HttpStatus() {
    }
}
