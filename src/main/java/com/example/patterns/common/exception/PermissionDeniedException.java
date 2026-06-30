package com.example.patterns.common.exception;

/**
 * 权限不足异常。
 *
 * <p>用于删除等敏感操作在权限校验未通过时抛出（由简化权限校验机制中的
 * {@code PermissionAspect} 在目标方法执行前触发）。抛出后目标数据保持不变，
 * 并由全局异常处理器 {@code GlobalExceptionHandler} 转换为
 * {@code AjaxResult.error(...)} 错误响应，向调用方返回指示权限不足的可观察结果
 * （满足需求 9.10、9.11、11.3）。</p>
 *
 * <p>继承设计说明：本异常选择继承 {@link ServiceException} 而非直接继承
 * {@link RuntimeException}，原因在于“权限不足”在语义上是“业务异常”的一种特化
 * （is-a 关系成立，满足里氏替换原则）。如此设计可复用 {@link ServiceException}
 * 统一的错误码与消息模型，并使本异常在全局异常处理器未单独注册其处理器时，
 * 仍能落入业务异常分支被兜底处理，避免逃逸为未预期异常。</p>
 *
 * @since 1.0.0
 */
public class PermissionDeniedException extends ServiceException {

    /** 序列化版本号。 */
    private static final long serialVersionUID = 1L;

    /** 默认错误消息：当调用方未指定具体消息时使用。 */
    private static final String DEFAULT_MESSAGE = "无操作权限";

    /**
     * 使用默认错误消息“无操作权限”构造权限不足异常。
     */
    public PermissionDeniedException() {
        super(DEFAULT_MESSAGE);
    }

    /**
     * 使用自定义错误消息构造权限不足异常。
     *
     * @param message 错误消息，建议包含被拒绝的操作或所需权限标识以便定位
     */
    public PermissionDeniedException(String message) {
        super(message);
    }
}
