package com.example.patterns.common.exception;

/**
 * 通用业务异常。
 *
 * <p>用于承载演示工程中各类可预期的业务失败场景，例如：未知支付渠道标识、
 * 容器中无匹配的可注入实现、限流超过阈值、建造者必选部件缺失等。该异常最终由
 * 全局异常处理器 {@code GlobalExceptionHandler} 统一拦截并转换为
 * {@code AjaxResult.error(...)} 形式的可观察错误响应，从而保证“非静默失败”
 * （满足需求 6.6、11.3）。</p>
 *
 * <p>设计说明：</p>
 * <ul>
 *     <li>继承 {@link RuntimeException}，属非受检异常，使 Controller 与 Service
 *         无需在方法签名上显式声明，业务异常可自然向上冒泡至全局异常处理器，
 *         配合 Controller 内严禁 try-catch 的约定（C6）。</li>
 *     <li>错误消息直接复用父类 {@link Throwable} 的 message 机制（经构造方法
 *         {@code super(message)} 传入，可通过 {@link #getMessage()} 获取），
 *         不再重复定义独立的 message 字段，以避免与父类状态产生冗余或不一致。</li>
 *     <li>可选的业务错误码由 {@link #code} 字段承载；未显式指定时为 {@code null}，
 *         由全局异常处理器决定采用的默认响应码。</li>
 * </ul>
 *
 * @since 1.0.0
 */
public class ServiceException extends RuntimeException {

    /** 序列化版本号。 */
    private static final long serialVersionUID = 1L;

    /**
     * 业务错误码（可选）。
     *
     * <p>为 {@code null} 时表示调用方未显式指定错误码，此时由全局异常处理器采用默认响应码。</p>
     */
    private Integer code;

    /**
     * 使用错误消息构造业务异常。
     *
     * @param message 业务错误消息，用于向调用方说明失败原因
     */
    public ServiceException(String message) {
        super(message);
    }

    /**
     * 使用业务错误码与错误消息构造业务异常。
     *
     * @param code    业务错误码，用于标识具体的业务失败类别
     * @param message 业务错误消息，用于向调用方说明失败原因
     */
    public ServiceException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 获取业务错误码。
     *
     * @return 业务错误码；当调用方未显式指定时返回 {@code null}
     */
    public Integer getCode() {
        return code;
    }
}
